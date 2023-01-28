(ns poly.web.rest-api.core
  (:require
   [integrant.core :as ig]
   [io.pedestal.http :as http]
   [io.pedestal.http.body-params :as body-params]
   [poly.web.config.interface :as cfg]
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
  [pool]
  [(body-params/body-params)
   m/coerce-body
   m/content-neg-intc
   (m/db-interceptor pool)
   m/err-handler])

(defn table-exists?
  [table-name]
  (select [[:exists (-> (select)
                        (from [:information_schema.tables])
                        (where [:and
                                [:= :table_schema "public"]
                                [:= :table_name table-name]]))]]))

(defmethod ig/init-key ::service-map [_ {:keys [pool extras]}]
  (sql/query (table-exists? "users") {} pool)
  (-> service
      (merge extras)
      http/default-interceptors
      (update ::http/interceptors into (common-interceptors pool))))

(defn start
  [service-map]
  (-> service-map
      http/create-server
      http/start))

(defmethod ig/init-key ::server [_ {:keys [service-map]}]
  (start service-map))

(defmethod ig/halt-key! ::server [_ server]
  (http/stop server))

(defn parse-cfgs
  "Parse all configs requested in `cfgs` into a single merged map."
  [cfgs opts]
  (reduce merge (map #(cfg/config % opts)
                     cfgs)))

(defn -main
  "The entry-point for 'clojure -M:main'"
  [& args]
  (let [component-cfgs ["sql/config.edn" "auth/config.edn" "rest-api/config.edn"]]
    (-> (parse-cfgs component-cfgs {:profile :default})
        cfg/config
        cfg/init)))
