(ns poly.web.config.interface-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :as test :refer [deftest is]]
   [poly.web.config.interface :as config]))

(deftest dev-profile
  (is (= {:debug true}
         (config/parse (io/resource "config/config.edn") {:profile :dev}))))

(deftest default-profile
  (is (= {:debug false}
         (config/parse (io/resource "config/config.edn") {:profile :default}))))
