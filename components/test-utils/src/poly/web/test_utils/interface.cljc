(ns poly.web.test-utils.interface
  (:require
   [poly.web.test-utils.core :as core]))

(defn set-log-config
  "Set the logging configuration for a given test run.

  By default, database pool and migration logs
  (`com.zaxxer.*` and `migratus.*`, respectively)
  are filtered below the `:error` level.

  Calling with your own `config` will override these settings."
  ([]
   (core/set-log-config))
  ([config]
   (core/set-log-config config)))

(defn sys-cfg
  "Parse all configs requested in `cfgs` into a single merged map."
  ([cfgs]
   (sys-cfg cfgs {:profile :dev}))
  ([cfgs opts]
   (core/sys-cfg cfgs opts)))

(defn pretty-spec!
  "Enable pretty printing for spec errors."
  [f]
  (core/pretty-spec! f))

(defn with-db!
  "Setup/teardown test DB."
  [db-name sys-cfg]
  (core/with-db! db-name sys-cfg))

(defn reset-migrations!
  "Reset any DB migrations between test runs."
  [db-name sys-cfg migration-cfg]
  (core/reset-migrations! db-name sys-cfg migration-cfg))
