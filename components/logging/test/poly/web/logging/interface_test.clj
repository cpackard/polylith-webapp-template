(ns poly.web.logging.interface-test
  (:require
   [clojure.test :as test :refer [deftest is testing]]
   [poly.web.logging.interface :as logging]))

(deftest info-logs
  (testing "can log a standalone message"
    (is (nil? (logging/info "info test"))))
  (testing "can log a message and args"
    (is (nil? (logging/info "info test" :with "extra message")))))

(deftest warn-logs
  (testing "can log a standalone message"
    (is (nil? (logging/warn "warn test"))))
  (testing "can log a message with args"
    (is (nil? (logging/warn "warn test" :data {:this "can be" :any "data"})))))

(deftest error-logs
  (testing "can log a standalone message"
    (is (nil? (logging/error "error test"))))
  (testing "can log a messag with args"
    (is (nil? (logging/error "error test"
                             :exception
                             (Exception. "exception which caused this log"))))))
