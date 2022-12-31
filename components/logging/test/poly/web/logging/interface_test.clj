(ns poly.web.logging.interface-test
  (:require
   [clojure.test :as test :refer :all]
   [poly.web.logging.interface :as logging]))

(deftest info-logs
  (is (nil? (logging/info "info test"))))

(deftest warn-logs
  (is (nil? (logging/warn "warn test"))))

(deftest error-logs
  (is (nil? (logging/error "error test"))))
