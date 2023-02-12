(ns poly.web.rest-api.handler
  (:require
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [io.pedestal.http.route :as route]
   [poly.web.rest-api.spec :as api-sp]
   [poly.web.user.interface :as user]
   [poly.web.user.interface.spec :as user-s]))

(s/fdef response
  :args (s/cat :status ::api-sp/status
               :body ::api-sp/body
               :headers ::api-sp/headers)

  :ret ::api-sp/http-response

  :fn (fn [{:keys [args ret]}]
        (let [status-eq  (= (:status args) (:status ret))
              body-eq    (= (:body args) (:body ret))
              headers-eq (every? (fn [[k v]] (= v (get-in ret [:headers k])))
                                 (partition 2 (:headers args)))]
          (and status-eq body-eq headers-eq))))

(defn- response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok (partial response 200))
(def created (partial response 201))
(def not-found (partial response 404))
(def forbidden (partial response 403))
(def bad-request (partial response 400))
(def bad-entity (partial response 422))

(defn- qualify-kws
  "Qualify all keys in `m` with the namespace `ns`"
  [m ns]
  (->> m
       (map (fn [[k v]] [(keyword ns (name k)) v]))
       (into {})))

(defn- explain-bad-req
  "Create an error map explaining why `form` is an invalid `spec`."
  [spec form]
  (when-not (s/valid? spec form)
    {:errors {:request-err [(expound/expound-str spec form)]}}))

(def user-register
  "PUT request to create a new user."
  {:name :user-register
   :enter
   (fn [context]
     (let [new-user (-> (get-in context [:request :json-params :user])
                        (qualify-kws (namespace ::user-s/id)))]
       (if-let [expl (explain-bad-req ::user/new-user new-user)]
         (assoc context :response (bad-entity expl))
         (let [{:keys [ds env]} (:request context)
               user             (user/register! new-user (:secret env) ds)
               url              (route/url-for :user-info
                                               :params {:user-id (::user-s/id user)})]
           (assoc context :response (created user "Location" url))))))})

(def user-login
  "POST request to login as an existing user."
  {:name :user-login
   :enter
   (fn [context]
     (let [{:keys [ds env json-params]} (:request context)
           {:keys [email password]}     (:user json-params)
           {:keys [errors] :as user}    (user/login email password (:secret env) ds)
           response-code                (if errors bad-request ok)]
       (assoc context :response (response-code user))))})

(def user-info
  "Retrieve all relevant info for a given user."
  {:name :user-info
   :enter
   (fn [context]
     (let [{:keys [ds env path-params auth-user]} (:request context)
           {::user-s/keys [token id]}             auth-user

           req-id (-> path-params :user-id Integer/parseInt)]
       (if-not (= id req-id)
         (assoc context :response (forbidden {:errors {:auth ["Permission denied."]}}))
         (let [user (user/user-by-token token (:secret env) ds)]
           (assoc context :response (ok user))))))})

(def echo
  "Simple handler for endpoint which always returns OK."
  {:name :echo
   :enter
   (fn [context]
     (let [response (ok "hi")]
       (assoc context :response response)))})
