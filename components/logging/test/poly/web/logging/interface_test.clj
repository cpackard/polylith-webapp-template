(ns poly.web.logging.interface-test
  (:require
   [clojure.test :as test :refer [deftest is testing use-fixtures]]
   [poly.web.logging.interface :as log]
   [poly.web.logging.test-utils :as log-tu]))

(let [log-cfg {:min-level [[#{"poly.web.log.*"} :trace]]}]
  (use-fixtures :once
    (log-tu/set-log-config log-cfg)))

(deftest info-logs
  (testing "can log a standalone message"
    (is (nil? (log/info "info test"))))
  (testing "can log a message and args"
    (is (nil? (log/info "info test" :with "extra message")))))

(deftest warn-logs
  (testing "can log a standalone message"
    (is (nil? (log/warn "warn test"))))
  (testing "can log a message with args"
    (is (nil? (log/warn "warn test" :data {:this "can be" :any "data"})))))

(deftest error-logs
  (testing "can log a standalone message"
    (is (nil? (log/error "error test"))))
  (testing "can log a messag with args"
    (is (nil? (log/error "error test"
                         :exception
                         (Exception. "exception which caused this log"))))))
