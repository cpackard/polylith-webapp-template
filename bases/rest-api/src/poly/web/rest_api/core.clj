(ns poly.web.rest-api.core
  (:require
   [clojure.java.io :as io]
   [io.pedestal.http :as http]
   [io.pedestal.http.body-params :as body-params]
   [io.pedestal.http.route :as route]
   [poly.web.config.interface :as cfg]
   [poly.web.logging.interface :as log]
   [poly.web.rest-api.api :as api]
   [poly.web.rest-api.middleware :as m]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.helpers :refer [from select where]])
  (:gen-class))

(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes api/routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false
                                        ;; Alternatively, You can specify you're own Jetty HTTPConfiguration
                                        ;; via the `:io.pedestal.http.jetty/http-configuration` container option.
                                        ;:io.pedestal.http.jetty/http-configuration (org.eclipse.jetty.server.HttpConfiguration.)
                                        }})

(defn- common-interceptors
  [pool env]
  [m/err-handler
   (body-params/body-params)
   m/coerce-body
   m/content-neg-intc
   (m/db-interceptor pool)
   (m/env-interceptor env)])

(defn table-exists?
  [table-name]
  (select [[:exists (-> (select)
                        (from [:information_schema.tables])
                        (where [:and
                                [:= :table_schema "public"]
                                [:= :table_name table-name]]))]]))

(defn service-map
  [pool env extras reloadable?]
  (sql/query (table-exists? "users") {} pool) ; run to initialize DB connection
  (-> service
      (merge extras (when reloadable? {::http/routes #(route/expand-routes api/routes)}))
      http/default-interceptors
      (update ::http/interceptors into (common-interceptors pool env))))

(defn start
  [service-map]
  (-> service-map
      http/create-server
      http/start))

(defn stop
  [server]
  (http/stop server))

(defn -main
  "The entry-point for 'clojure -M:main'"
  [& args]
  (when-let [m (sql/pending-migrations)]
    (log/info (str "You have " (count m) " unapplied migrations.")))
  (-> (io/resource "rest-api/config.edn")
      (cfg/parse {:profile :default})
      cfg/init))
