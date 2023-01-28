(ns poly.web.rest-api.core_test
  (:require
   [clojure.data.json :as json]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [io.pedestal.http :as http]
   [io.pedestal.test :refer [response-for]]
   [poly.web.config.interface :as cfg]
   [poly.web.logging.interface.test-utils :as log-tu]
   [poly.web.rest-api.core :as core]
   [poly.web.sql.interface.test-utils :as sql-tu]
   [poly.web.test-utils.interface :as test-utils]
   [poly.web.user.interface :as user]))

(let [test-db "poly_web_rest_api_core_test"]
  (use-fixtures :once
    (log-tu/set-log-config)
    (sql-tu/with-db! test-db)
    test-utils/pretty-spec!)

  (use-fixtures :each
    (sql-tu/reset-migrations! test-db)))

; TODO: clean this up (see below)
(defn service
  []
  (-> ["sql/config.edn" "auth/config.edn" "rest-api/config.edn"]
      (core/parse-cfgs {:profile :test})
      (dissoc ::core/server)
      cfg/init
      ::core/service-map
      http/create-servlet
      ::http/service-fn))

(deftest echo-route--ok-response
  (is (= 200 (:status (response-for (service) :get "/api/echo")))))

; TODO: should bricks expose helper functions to make test creation easier?
; like `poly.web.user.interface.test`?
;; (comment
;;   (require '[poly.web.user.interface.test :as user-t])
;;   (deftest user
;;     (let [user (gen/generate (s/gen ::user-t/registered-user))])))

(deftest user
  (let [user (gen/generate (s/gen ::user/new-user))]
    (testing "can create successfully"
      (let [{:keys [status]}
            (response-for (service)
                          :post "/api/users"
                          :body (json/write-str {:user user})
                          :headers {"Content-Type" "application/json"})]
        (is (= 200 status)))))
  (testing "receives correct error message"))
