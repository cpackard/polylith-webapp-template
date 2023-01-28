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
  [data]
  (->> (partial mapv as-pprint-str)
       (update data :vargs)))

(def default-config
  {:ns-filter {:deny #{"com.zaxxer.*" "migratus.*"}}
   :middleware [maybe-pprint]})

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
