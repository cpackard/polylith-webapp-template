(ns poly.web.logging.interface.test-utils
  (:require
   [poly.web.logging.interface :as log]))

(defn set-log-config
  "Set the logging configuration for a given test run.

  By default, database pool and migration logs
  (`com.zaxxer.*` and `migratus.*`, respectively) are filtered.

  Calling with your own `config` will override these settings."
  [& config]
  (let [default-config {:ns-filter {:deny #{"com.zaxxer.*" "migratus.*"}}}]
    (fn [f]
      (log/with-merged-config (apply merge default-config config)
        (f)))))
