#_{:clj-kondo/ignore [:unused-namespace :unused-referred-var]}
(ns integrant
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as st]
   [clojure.string :as str]
   [expound.alpha :as expound]
   [integrant.core :as ig]
   [integrant.repl :refer [clear go halt init prep reset reset-all]]
   [integrant.repl.state :refer [system]]
   [poly.web.config.interface :as cfg]
   [poly.web.sql.interface :as sql]))

(integrant.repl/set-prep!
 (fn [] (cfg/parse-cfgs [(io/resource "rest-api/config.edn")] {:profile :dev})))

(defn get-ds
  "Retrieve the initialized connection pool from Integrant."
  []
  (::sql/db-pool system))

(defn setup
  []
    ;; Human-readable spec errors
    ;; see the docs for details: https://github.com/bhb/expound/blob/master/doc/faq.md#how-do-i-use-expound-to-print-all-spec-errors
  (set! s/*explain-out* expound/printer)

    ;; Enable instrumentation for all registered `spec`s
  (st/unstrument)
  (st/instrument)

    ;; start Integrant system
  (go))

(comment
  ;; (re)start system
  (setup))
