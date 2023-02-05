(ns poly.web.sql.test-utils
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [poly.web.config.interface :as cfg]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.migrations :as migrations]))

(defn- create-sys-cfg
  "Merge all configs needed for creating the test DB."
  [& {:keys [configs]
      :or {configs (io/resource "rest-api/config.edn")}}]
  (-> configs
      (cfg/parse {:profile :test})
      (select-keys [::sql/db-pool ::sql/db-spec])))

(defn- start-db-pool!
  "Initialize DB connection for tests."
  [db-name sys-cfg]
  (-> sys-cfg
      (assoc-in [::sql/db-spec :dbname] db-name)
      cfg/init))

(defn migration-cfg
  [db-name]
  (assoc-in migrations/config [:db :dbname] db-name))

(defonce ^:private test-ds
  (atom nil))

(defn ds [] @test-ds)

(s/def ::db-name string?)
(s/def ::opts map?)
(s/fdef with-db!
  :args (s/keys :opt-un [::opts ::db-name]))

(defn with-db!
  "Setup/teardown a test DB.

  Also resets the underlying connection pool for the `ds` function."
  [db-name & {:keys [opts]
              :or   {opts {}}}]
  (fn [f]
    (let [sys-cfg   (create-sys-cfg)
          ds        (::sql/db-spec sys-cfg)
          create-db {:raw (str "CREATE DATABASE " db-name)}
          drop-db   {:raw (str "DROP DATABASE IF EXISTS " db-name " WITH (FORCE)")}]
      (sql/query drop-db ds opts)
      (sql/query create-db ds opts)

      (let [sys (start-db-pool! db-name sys-cfg)]
        (reset! test-ds (::sql/db-pool sys))
        (try
          (f)
          (finally
            (reset! test-ds nil)
            (sql/query drop-db ds opts)
            (cfg/halt! sys)))))))

(s/fdef reset-migrations!
  :args (s/cat :db-name string?))

(defn reset-migrations!
  "Reset DB migrations between test runs."
  [db-name]
  (fn [f]
    (migrations/reset-migrations! (migration-cfg db-name))
    (f)))

(defn migration-cleanup!
  "Delete the SQL migration table
  and all `.edn` files in the migration directory."
  ([]
   (migration-cleanup! migrations/config))
  ([config]
   (let [migration-table (keyword (:migration-table-name config))
         ds              (:db config)
         drop-table      #(sql/query {:drop-table [:if-exists migration-table]} ds)
         delete-fs       #(doseq [file (migrations/migration-files config)]
                            (when (string/ends-with? file ".edn")
                              (io/delete-file file true)))]
     (fn [f]
       (doseq [g [drop-table delete-fs]] (g))

       (f)

       (doseq [g [drop-table delete-fs]] (g))))))
