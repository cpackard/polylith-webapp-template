(ns poly.web.rest-api.core_test
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.string :as string]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [io.pedestal.http :as http]
   [io.pedestal.test :refer [response-for]]
   [poly.web.config.interface :as cfg]
   [poly.web.logging.interface.test-utils :as log-tu]
   [poly.web.rest-api.interface :as rest-api]
   [poly.web.spec.test-utils :as spec-tu]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.test-utils :as sql-tu]
   [poly.web.test-utils.interface :as test-utils]
   [poly.web.user.interface :as user]
   [poly.web.user.interface.spec :as user-s]
   [poly.web.user.interface.test-utils :as user-tu]))

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

; TODO: should bricks expose helper functions to make test creation easier?
; like `poly.web.user.interface.test`?
;; (comment
;;   (require '[poly.web.user.interface.test :as user-t])
;;   (deftest user
;;     (let [user (gen/generate (s/gen ::user-t/registered-user))])))

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

;; TODO: I don't like the look of these nested tests 🤔
(deftest user
  (testing "/api/users"
    (let [user (gen/generate (s/gen ::user/new-user))]
      (testing "can register a user successfully"
        (let [{:keys [status]}
              (res-for :post "/api/users" :body {:user user})]
          (is (= 200 status))))
      (testing "handles bad request (missing email)"
        (let [{:keys [status body]}
              (res-for :post "/api/users" :body {:user (dissoc user ::user-s/email)})]
          (is (string/includes? body "should contain key: :poly.web.user.interface.spec\\/email"))
          (is (= 422 status)))))))

(deftest user-login--can--login--with--registered--user
  (testing "/api/users/:user-id/login"
    (let [email (spec-tu/gen-email)
          user  (user-tu/new-user! (sql-tu/ds) ::user-s/email email)]
      (testing "can login with a registered user"
        (let [{:keys [status body]}
              (res-for :post (str "/api/users/" (::user-s/id user) "/login")
                       :body {:user user})]
          (is (= 200 status)
              (str "/api/users/" (::user-s/id user) "/login"))
          (is (some? (-> body json/read-str (get "token")))))))))

(deftest user-info
  (testing "receives an Unauthorized error without authentication"
    (let [{:keys [status body]} (res-for :get "/api/users/1")]
      (is (= 401 status))
      (is (= {"errors" {"auth" ["Authorization required."]}}
             (json/read-str body)))))
  (testing "receives a Forbidden error with insufficient permissions"
    (let [email                      (spec-tu/gen-email)
          {::user-s/keys [token id]} (user-tu/new-user! (sql-tu/ds) ::user-s/email email)
          {:keys [status body]}      (res-for :get (str "/api/users/" (inc id))
                                              :token token)]
      (is (= 403 status))
      (is (= {"errors" {"auth" ["Permission denied."]}}
             (json/read-str body)))))
  (testing "can successfully read user info"
    (let [email
          (spec-tu/gen-email)

          {::user-s/keys [token id name email username]}
          (user-tu/new-user! (sql-tu/ds) ::user-s/email email)

          {:keys [status body]}
          (res-for :get (str "/api/users/" id) :token token)]
      (is (= 200 status))
      (is (= {:id id :name name :email email :username username :token token}
             (json/read-str body :key-fn keyword))))))
