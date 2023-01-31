#_{:clj-kondo/ignore [:unused-namespace :unused-referred-var]}
(ns user
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as st]
   [expound.alpha :as expound]
   [integrant.core :as ig]
   [integrant.repl :refer [clear go halt init prep reset reset-all]]
   [integrant.repl.state :refer [system]]
   [io.pedestal.http.route :as route]
   [io.pedestal.http.route.definition.table :refer [table-routes]]
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
   [poly.web.logging.interface
    [test-utils :as log-tu]]
   [poly.web.rest-api
    [handler :as handler]
    [api :as api]
    [interface :as rest-api]
    [middleware :as middleware]
    [core :as api-core]
    [spec :as api-spec]]
   [poly.web.spec
    [test-utils :as spec-tu]
    [interface :as spec]
    [core :as spec-core]]
   [poly.web.sql
    [core :as sql-core]
    [interface :as sql]
    [migrations :as sql-m]]
   [poly.web.sql.interface
    [helpers :as sql-h]
    [spec :as sql-s]
    [test-utils :as sql-tu]]
   [poly.web.test-utils
    [core :as test-utils-core]
    [interface :as test-utils]]
   [poly.web.user
    [core :as user-core]
    [interface :as user]
    [store :as user-store]]
   [poly.web.user.interface
    [spec :as user-s]
    [test-utils :as user-tu]]))

(integrant.repl/set-prep!
 (fn [] (cfg/parse-cfgs [(io/resource "rest-api/config.edn")] {:profile :dev})))

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

(defn get-ds
  "Retrieve the initialized connection pool from Integrant."
  []
  (::sql/db-pool system))

;; Utility functions from http://pedestal.io/guides/developing-at-the-repl#developing-at-the-repl

(defn print-routes
  "Print our application's routes"
  []
  (route/print-routes (table-routes api/routes)))

(defn named-route
  "Finds a route by name"
  [route-name]
  (->> api/routes
       table-routes
       (filter #(= route-name (:route-name %)))
       first))

(defn print-route
  "Prints a route and its interceptors"
  [rname]
  (letfn [(joined-by
            [s coll]
            (apply str (interpose s coll)))

          (repeat-str
            [s n]
            (apply str (repeat n s)))

          (interceptor-info
            [i]
            (let [iname  (or (:name i) "<handler>")
                  stages (->> (dissoc i :name)
                              (filter (comp (complement nil?) val))
                              keys
                              (joined-by ","))]
              (str iname " (" stages ")")))]
    (when-let [rte (named-route rname)]
      (let [{:keys [path method route-name interceptors]} rte
            name-line (str "[" method " " path " " route-name "]")]
        (->> (into [name-line (repeat-str "-" (count name-line))]
                   (map interceptor-info interceptors))
             (joined-by "\n")
             println)))))

(defn recognize-route
  "Verifies the requested HTTP verb and path are recognized by the router."
  [verb path]
  (route/try-routing-for (table-routes api/routes) :prefix-tree path verb))

(comment
  ;; (re)start system
  (setup)

  ;; print all routes
  (print-routes)

  ;; find a specific route
  (named-route :user-register)

  ;; get detailed route info by handler name
  (print-route :user-register)

  ;; get detailed route info by verb and relative path
  (recognize-route :post "/api/users"))
