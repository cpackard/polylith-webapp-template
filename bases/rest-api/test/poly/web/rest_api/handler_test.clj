(ns poly.web.rest-api.handler-test
  (:require
   [clojure.test :as test :refer [deftest is]]
   [io.pedestal.interceptor :refer [interceptor]]
   [io.pedestal.interceptor.chain :as chain]
   [poly.web.rest-api.handler :as handler]))

(deftest echo-route
  (let [response (chain/execute {} [(interceptor handler/echo)])]
    (is (= 200 (get-in response [:response :status])))))
