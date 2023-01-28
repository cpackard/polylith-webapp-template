(ns poly.web.rest-api.handler
  (:require
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
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
(def not-found (partial response 404))
(def bad-request (partial response 400))
(def bad-entity (partial response 422))

(defn- response-code
  "Choose the appropriate response code function for the given response."
  [{:keys [errors] :as res}]
  (cond
    (:request-err errors) (bad-entity res)
    errors (bad-request res)
    :else (ok res)))

(defn- qualify-kws
  "Qualify all keys in `m` with the namespace `ns`"
  [m ns]
  (->> m
       (map (fn [[k v]] [(keyword ns (name k)) v]))
       (into {})))

(defn- explain-bad-req
  "Create an error map explaining why `form` is an invalid `spec`."
  [spec form]
  {:errors {:request-err [(expound/expound spec form)]}})

;; TODO: update this to be PUT
;; and make use of `url-for` http://pedestal.io/guides/your-first-api#_url_for
(def user-register
  "POST request to create a new user."
  {:name :user-register
   :enter
   (fn [context]
     (let [new-user (-> (get-in context [:request :json-params :user])
                        (qualify-kws (namespace ::user-s/id)))
           response (if (s/valid? ::user/new-user new-user)
                      (->> new-user user/register!)
                      (explain-bad-req ::user/new-user new-user))]
       (assoc context :response (response-code response))))})

(def user-login
  "POST request to login as an existing user."
  {:name :user-login
   :enter
   (fn [context]
     (let [{:keys [email password]} (:request context)]
       (->> (user/login email password)
            response-code
            (assoc context :response))))})

(def echo
  "Simple handler for endpoint which always returns OK."
  {:name :echo
   :enter
   (fn [context]
     (let [response (ok "hi")]
       (assoc context :response response)))})
