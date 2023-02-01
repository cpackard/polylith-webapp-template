(ns poly.web.rest-api.middleware
  (:require
   [clojure.data.json :as json]
   [clojure.string :as string]
   [io.pedestal.http.content-negotiation :as conneg]
   [io.pedestal.interceptor :refer [interceptor]]
   [io.pedestal.interceptor.error :as err]
   [poly.web.sql.interface :as sql]
   [poly.web.user.interface :as user]))

(def supported-types ["text/html"
                      "application/edn"
                      "application/json"
                      "text/plain"])

(def content-neg-intc (conneg/negotiate-content supported-types))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/html"        body
    "text/plain"       body
    "application/edn"  (pr-str body)
    "application/json" (json/write-str body)))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def coerce-body
  (interceptor
   {:name ::coerce-body
    :leave
    (fn [context]
      (cond-> context
        (nil? (get-in context [:response :headers "Content-Type"]))
        (update-in [:response] coerce-to (accepted-type context))))}))

(defn db-interceptor
  "Attaches the database pool to each request.

  If `:tx-data` is present on the `:leave` context,
  each query in the collection is executed in a transaction."
  [pool]
  (interceptor
   {:name ::database-interceptor
    :enter
    (fn [context]
      (update context :request assoc :ds pool))
    :leave
    (fn [context]
      (if-let [queries (:tx-data context)]
        (sql/transaction pool queries)
        context))}))

(defn env-interceptor
  "Attaches all environment info to the request."
  [env]
  (interceptor
   {:name ::env-interceptor
    :enter
    (fn [context]
      (update context :request assoc :env env))}))

(def wrap-auth-user
  "Interceptor for adding the calling users' info
  to the request context if the provided auth token is valid."
  (interceptor
   {:name ::wrap-auth-user
    :enter
    (fn [context]
      (let [auth             (get-in context [:request :headers "authorization"] "")
            token            (-> auth (string/split #" ") last)
            {:keys [ds env]} (:request context)]
        (if (string/blank? token)
          context
          (let [{:keys [errors] :as user} (user/user-by-token token (:secret env) ds)]
            (if errors
              context
              (update context :request assoc :auth-user user))))))}))

(def wrap-authorization
  "Interceptor to guard against unauthorized route access."
  (interceptor
   {:name ::wrap-authorization
    :enter
    (fn [context]
      (if (get-in context [:request :auth-user])
        context
        (let [auth-err {:errors {:auth ["Authorization required."]}}]
          (assoc context :response {:status 401 :body auth-err :headers {}}))))}))

(def err-handler
  (err/error-dispatch
   #_{:clj-kondo/ignore [:unresolved-symbol]}
   [ctx ex]

   [{:exception-type :java.lang.NumberFormatException}]
   #_{:clj-kondo/ignore [:unresolved-symbol]}
   (assoc ctx :response {:status 400 :body "Not a number!\n"})

   [{:exception-type :com.fasterxml.jackson.core.JsonParseException
     :interceptor    :io.pedestal.http.body-params/body-params}]
   #_{:clj-kondo/ignore [:unresolved-symbol]}
   (assoc ctx :response {:status 400 :body (.getMessage ex)})

   :else
   #_{:clj-kondo/ignore [:unresolved-symbol]}
   (assoc ctx :io.pedestal.interceptor.chain/error ex)))
