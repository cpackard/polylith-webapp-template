(ns poly.web.logging.interface.test-utils
  (:require
   [clojure.pprint :as pp]
   [poly.web.logging.interface :as log]))

(defn- as-pprint-str
  [e]
  (if (string? e)
    e
    (with-out-str (pp/pprint e))))

(defn- maybe-pprint
  "Convert log data into a pretty-print string, if applicable."
  [data]
  (->> (partial mapv as-pprint-str)
       (update data :vargs)))

(def ^:private silenced-ns
  #{"com.zaxxer.*" "migratus.*" "io.pedestal.*"})

(def default-config
  {:min-level [[silenced-ns :error]]
   :middleware [maybe-pprint]})

(defn set-log-config
  "Set the logging configuration for a given test run.

  By default, database, migration, and server logs
  (`com.zaxxer.*`, `migratus.*`, and `io.pedestal.*` respectively)
  are filtered below the `:error` level.

  Calling with your own `config` will override these settings."
  [& config]
  (fn [f]
    (log/with-merged-config (apply merge default-config config)
      (f))))
