#_{:clj-kondo/ignore [:unused-namespace :unused-referred-var]}
(ns user
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as st]
   [expound.alpha :as expound]
   [integrant.core :as ig]
   [integrant.repl :refer [clear go halt init prep reset reset-all]]
   [integrant.repl.state :refer [system]]
   [poly.web.auth
    [core :as auth-core]
    [interface :as auth]]
   [poly.web.auth.interface
    [spec :as auth-spec]]
   [poly.web.config
    [core :as cfg-core]
    [interface :as cfg]
    [spec :as cfg-sp]]
   [poly.web.logging
    [core :as log-core]
    [interface :as log]]
   [poly.web.spec
    [interface :as spec]
    [core :as spec-core]]
   [poly.web.sql
    [core :as sql-core]
    [interface :as sql]
    [migratus :as sql-mig]
    [spec :as sql-sp]]
   [poly.web.sql.interface
    [helpers :as sql-helpers]
    [spec :as sql-spec]]
   [poly.web.test-utils
    [core :as test-utils-core]
    [interface :as test-utils]]
   [poly.web.user
    [core :as user-core]
    [interface :as user]
    [store :as user-store]]
   [poly.web.user.interface
    [spec :as user-spec]]))

(def sys-cfg (let [configs (map (fn [comp-cfg]
                                  (cfg/config comp-cfg {:profile :dev}))
                                ["sql/config.edn"
                                 "auth/config.edn"])]
               (apply merge configs)))

(def ^:private sys
  "reference to initialized dependency state"
  (atom nil))

(integrant.repl/set-prep!
 (fn [] sys-cfg))

(defn setup
  []
  ;; Human-readable spec errors
  ;; see the docs for details: https://github.com/bhb/expound/blob/master/doc/faq.md#how-do-i-use-expound-to-print-all-spec-errors
  (set! s/*explain-out* expound/printer)

  ;; Enable instrumentation for all registered `spec`s
  (st/unstrument)
  (st/instrument)

  ;; start Integrant system

  (when @sys
    (cfg/halt! @sys))
  (reset! sys (cfg/init sys-cfg))
  ;(go)
  )

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
