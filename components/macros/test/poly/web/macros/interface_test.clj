(ns poly.web.macros.interface-test
  (:require
   [clojure.test :as test :refer [deftest is testing]]
   [poly.web.macros.interface :as macros]))

(def mock-ds "Mock var to act as another parameter" nil)

(deftest some-fn->
  (testing "can be called without forms"
    (is (= :a (macros/some-fn-> :b :a))))
  (testing "evaluates all forms if pred is never true"
    (is (= 3 (macros/some-fn-> :c {:a {:b 2}} :a :b inc))))
  (testing "returns the first value for which pred is true"
    (letfn [(has-email? [_ _]
              {:errors {:email ["Missing email."]}})]
      (is (= {:errors {:email ["Missing email."]}}
             (macros/some-fn-> :errors {:user {:email "a@b.com"}} :user (has-email? mock-ds) :email))))))
