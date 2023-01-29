(ns poly.web.sql.migratus
  "Namespace for the `migratus` config file."
  (:require
   [poly.web.config.interface :as cfg]))

(def config
  {:store                :database
   :migration-dir        "components/sql/resources/sql/migrations/"
   :migration-table-name "app_migrations"
   :db                   (-> (cfg/config "sql/config.edn")
                             :poly.web.sql.interface/db-spec)})

;; The actual config must be available at the namespace level
;; for `migratus` to find it.
config
