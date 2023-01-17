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
    [spec :as auth-s]]
   [poly.web.config
    [core :as cfg-core]
    [interface :as cfg]]
   [poly.web.config.interface
    [spec :as cfg-s]]
   [poly.web.logging
    [core :as log-core]
    [interface :as log]]
   [poly.web.spec
    [interface :as spec]
    [core :as spec-core]]
   [poly.web.sql
    [core :as sql-core]
    [interface :as sql]
    [migratus :as sql-m]]
   [poly.web.sql.interface
    [helpers :as sql-h]
    [spec :as sql-s]]
   [poly.web.test-utils
    [core :as test-utils-core]
    [interface :as test-utils]]
   [poly.web.user
    [core :as user-core]
    [interface :as user]
    [store :as user-store]]
   [poly.web.user.interface
    [spec :as user-s]]))

(def sys-cfg (let [configs (map (fn [comp-cfg]
                                  (cfg/config comp-cfg {:profile :dev}))
                                ["sql/config.edn"
                                 "auth/config.edn"])]
               (apply merge configs)))

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

  ;; Enable all logging
  (log/merge-config! {:min-level :debug})

  ;; start Integrant system
  (go))

(defn get-ds
  "Retrieve the initialized connection pool from Integrant."
  []
  (::sql/db-pool system))

(comment
  ;; (re)start system
  (setup))
