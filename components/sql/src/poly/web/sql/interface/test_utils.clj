(ns poly.web.sql.interface.test-utils
  (:require
   [clojure.spec.alpha :as s]
   [migratus.core :as migratus]
   [poly.web.config.interface :as cfg]
   [poly.web.sql.interface :as sql]
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
      (migratus/reset migration-cfg)

      (f)

      (cfg/halt! sys))))
