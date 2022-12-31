#_{:clj-kondo/ignore [:unused-namespace :unused-referred-var]}
(ns user
  (:require
   [clojure.spec.test.alpha :as st]
   [integrant.core :as ig]
   [integrant.repl :refer [clear go halt init prep reset reset-all]]
   [integrant.repl.state :refer [system]]
   [poly.web.config.core :as cfg-c]
   [poly.web.config.interface :as cfg]
   [poly.web.config.interface-test]
   [poly.web.config.spec :as cfg-s]
   [poly.web.logging.core :as log-c]
   [poly.web.logging.interface :as log]
   [poly.web.logging.interface-test]
   [poly.web.sql.core :as sql-c]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface-test]
   [poly.web.sql.spec :as sql-s]
   [poly.web.user.core :as user-c]
   [poly.web.user.interface :as user]))

(integrant.repl/set-prep! (fn []
                            (let [configs (map (fn [comp-cfg]
                                                 (cfg/config comp-cfg {:profile :dev}))
                                               ["sql/config.edn"
                                                "user/config.edn"])]
                              (apply merge configs))))

;; Enable instrumentation for all registered `spec`s
(st/instrument)

;; start Integrant system
(go)

(defn get-ds
  "Retrieve the initialized connection pool from Integrant."
  []
  (::sql/db-pool system))

(comment
  ;; start system
  (go)

  ;; stop system
  (halt)

  ;; reload source files and restart system
  (reset)

;; See https://github.com/weavejester/integrant-repl#usage for details
  )
