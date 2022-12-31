(ns poly.web.sql.migratus
  "Namespace for the `migratus` config file."
  (:require
   [poly.web.config.interface :as config]
   [poly.web.sql.interface :as sql]))

(def config
  {:store                :database
   :migration-dir        "components/sql/resources/sql/migrations/"
   :migration-table-name "app_migrations"
   :db                   (::sql/db-spec (config/config "sql/config.edn"))})

;; The actual config must be available at the namespace level
;; for `migratus` to find it.
config

(comment
  ;; NOTE: run these in a REPL to perform migration tasks
  (require '[migratus.core :as migratus])

  ;; Run `up` for any migrations that have not been run. Returns nil if successful.
  (migratus/migrate config)

  ;; Create a new migration with the current date.
  (migratus/create config "the-migration-name" :edn)

  ;; Run `down` for the last migration that was run.
  (migratus/rollback config)

  ;; Run `down` all migrations after migration-id.
  ;; This only considers completed migrations, and will not migrate up.

  ;; migration IDs are (by default) 14-digit timestamps so they have second-level granularity
  ;; to reduce the chance of collisions.
  (def migration-id "20111202091200")
  (migratus/rollback-until-just-after config migration-id)

  ;; Run `up` for the specified migration ids.
  ;; Will skip any migration that is already up.
  (migratus/up config [migration-id])

  ;; Run `down` for the specified migration ids.
  ;; Will skip any migration that is already down.
  (migratus/down config [migration-id])

  ;; Reset the database by `down`-ing all migrations successfully applied,
  ;; then `up`-ing all migrations.
  (migratus/reset config)

  ;; Returns a list of pending migrations.
  (migratus/pending-list config)

  ;; Run `up` for for any pending migrations which precede the given migration id
  ;; (good for testing migrations).
  (migratus/migrate-until-just-before config migration-id))
