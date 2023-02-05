(ns poly.web.rest-api.core_test
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.string :as string]
   [clojure.test :refer [deftest is use-fixtures]]
   [io.pedestal.http :as http]
   [io.pedestal.test :refer [response-for]]
   [poly.web.config.interface :as cfg]
   [poly.web.logging.test-utils :as log-tu]
   [poly.web.rest-api.interface :as rest-api]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.test-utils :as sql-tu]
   [poly.web.test-utils.interface :as test-utils]
   [poly.web.user.interface :as user]
   [poly.web.user.interface.spec :as user-s]
   [poly.web.user.test-utils :as user-tu]))

(def ^:private service
  "Reference to the HTTP service used during testing"
  (atom nil))

(defn- http-service
  "Create HTTP service function for testing"
  [db-name]
  (fn [f]
    (let [sys (-> [(io/resource "rest-api/config.edn")]
                  (cfg/parse-cfgs {:profile :test})
                  (dissoc ::rest-api/server)
                  (assoc-in [::sql/db-spec :dbname] db-name)
                  cfg/init)
          service-fn (-> sys ::rest-api/service-map http/create-servlet ::http/service-fn)]
      (reset! service service-fn)

      (f)

      (cfg/halt! sys)
      (reset! service nil))))

(let [test-db "poly_web_rest_api_core_test"]
  (use-fixtures :once
    (log-tu/set-log-config)
    (sql-tu/with-db! test-db)
    test-utils/pretty-spec!
    (http-service test-db))

  (use-fixtures :each
    (sql-tu/reset-migrations! test-db)))

(deftest echo-route--ok-response
  (is (= 200 (:status (response-for @service :get "/api/echo")))))

(defn- res-for
  "Helper function around `response-for`.

  Automatically adds necessary headers and converts `body` to JSON."
  [verb path & {:keys [body token]}]
  (let [headers (cond-> {"Content-Type" "application/json"
                         "Accept"       "application/json"}
                  token (merge {"Authorization" (str "Bearer " token)}))
        args    (cond-> [verb path]
                  body    (concat [:body (json/write-str body)])
                  :always (concat [:headers headers]))]
    (apply response-for @service args)))

(deftest user-register--bad-request
  (let [user (gen/generate (s/gen ::user/new-user))
        req  {:user (dissoc user ::user-s/email)}

        {:keys [status body]} (res-for :post "/api/users" :body req)]
    (is (string/includes? body "should contain key: :poly.web.user.interface.spec\\/email"))
    (is (= 422 status))))

(deftest user-register--success
  (let [user (gen/generate (s/gen ::user/new-user))

        {:keys [status]} (res-for :post "/api/users" :body {:user user})]
    (is (= 200 status))))

(comment
  (deftest user-register--all
    (user-register--bad-request)
    (user-register--success)))

(deftest user-login--success
  (let [user  (user-tu/new-user! (sql-tu/ds))
        path  (str "/api/users/" (::user-s/id user) "/login")

        {:keys [status body]} (res-for :post path :body {:user user})]
    (is (= 200 status))
    (is (some? (-> body json/read-str (get "token"))))))

(deftest user-info--unauthorized
  (let [{:keys [status body]} (res-for :get "/api/users/1")]
    (is (= 401 status))
    (is (= {"errors" {"auth" ["Authorization required."]}}
           (json/read-str body)))))

(deftest user-info--forbidden
  (let [{::user-s/keys [token id]} (user-tu/new-user! (sql-tu/ds))
        {:keys [status body]}      (res-for :get (str "/api/users/" (inc id))
                                            :token token)]
    (is (= 403 status))
    (is (= {"errors" {"auth" ["Permission denied."]}}
           (json/read-str body)))))

(deftest user-info--success
  (let [{::user-s/keys [token id name email username]}
        (user-tu/new-user! (sql-tu/ds))

        {:keys [status body]}
        (res-for :get (str "/api/users/" id) :token token)]
    (is (= 200 status))
    (is (= {:id id :name name :email email :username username :token token}
           (json/read-str body :key-fn keyword)))))

(comment
  (deftest user-info--all
    (user-info--unauthorized)
    (user-info--forbidden)
    (user-info--success)))
