(ns poly.web.sql.interface.test-utils
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [poly.web.config.interface :as cfg]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.migrations :as migrations]
   [poly.web.sql.migratus :as sql-m]))

(defn- create-sys-cfg
  "Merge all configs needed for creating the test DB."
  [& {:keys [configs]
      :or {configs ["sql/config.edn" "auth/config.edn"]}}]
  (cfg/parse-cfgs configs {:profile :test}))

(defn- start-db-pool!
  "Initialize DB connection for tests."
  [db-name sys-cfg]
  (let [sys (-> sys-cfg
                (assoc-in [::sql/db-spec :dbname] db-name)
                cfg/init)
        migratus-cfg (assoc-in sql-m/config [:db :dbname] db-name)]
    [sys migratus-cfg]))

(s/def ::db-name string?)
(s/def ::opts map?)
(s/fdef with-db!
  :args (s/keys :opt-un [::opts ::db-name]))

(defn with-db!
  "Setup/teardown a test DB."
  [db-name & {:keys [opts]
              :or {opts {}}}]
  (fn [f]
    (let [sys-cfg (create-sys-cfg)
          ds (::sql/db-spec sys-cfg)
          create-db {:raw (str "CREATE DATABASE " db-name)}
          drop-db   {:raw (str "DROP DATABASE IF EXISTS " db-name " WITH (FORCE)")}]
      (sql/query drop-db opts ds)
      (sql/query create-db opts ds)

      (f)

      (sql/query drop-db opts ds))))

(s/fdef reset-migrations!
  :args (s/cat :db-name string?))

(defn reset-migrations!
  "Reset DB migrations between test runs."
  [db-name]
  (fn [f]
    (let [[sys migration-cfg] (start-db-pool! db-name (create-sys-cfg))]
      (migrations/reset-migrations! migration-cfg)

      (f)

      (cfg/halt! sys))))

(defn migration-cleanup!
  "Delete the SQL migration table
  and all `.edn` files in the migration directory."
  ([]
   (migration-cleanup! sql-m/config))
  ([config]
   (let [migration-table (keyword (:migration-table-name config))
         ds              (:db config)
         drop-table      #(sql/query {:drop-table [:if-exists migration-table]} {} ds)
         delete-fs       #(doseq [file (migrations/migration-files config)]
                            (when (string/ends-with? file ".edn")
                              (io/delete-file file true)))]
     (fn [f]
       (doseq [f0 [drop-table delete-fs]] (f0))

       (f)

       (doseq [f2 [drop-table delete-fs]] (f2))))))
