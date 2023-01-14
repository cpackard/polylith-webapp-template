(ns poly.web.test-utils.core
  (:require
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [migratus.core :as migratus]
   [poly.web.config.interface :as cfg]
   [poly.web.logging.interface :as log]
   [poly.web.sql.interface :as sql]))

(defn set-log-config
  ([]
   (let [config {:min-level [[#{"com.zaxxer.*" "migratus.*"} :error]]}]
     (set-log-config config)))
  ([config]
   (fn [f]
     (log/merge-config! config)
     (f))))

(defn sys-cfg
  "Parse all configs requested in `cfgs` into a single merged map."
  [cfgs opts]
  (let [read-fn (fn [cfg] (cfg/config cfg opts))]
    (->> (map read-fn cfgs)
         (apply merge))))

(defn pretty-spec!
  "Enable pretty printing for spec errors."
  [f]
  (set! s/*explain-out* expound/printer)
  (f))

(defn with-db!
  "Setup/teardown test DB."
  [db-name sys-cfg]
  (fn [f]
    (let [ds        (::sql/db-spec sys-cfg)
          opts      {}
          create-db {:raw (str "CREATE DATABASE " db-name)}
          drop-db   {:raw (str "DROP DATABASE IF EXISTS " db-name " WITH (FORCE)")}]
      (sql/query drop-db opts ds)
      (sql/query create-db opts ds)

      (f)

      (sql/query drop-db opts ds))))

(defn reset-migrations!
  "Reset any DB migrations between test runs."
  [db-name sys-cfg migration-cfg]
  (fn [f]
    (let [test-sys     (-> sys-cfg
                           (assoc-in [::sql/db-spec :dbname] db-name)
                           cfg/init)
          migratus-cfg (assoc-in migration-cfg [:db :dbname] db-name)]

      (migratus/reset migratus-cfg)

      (f)
      (cfg/halt! test-sys))))
