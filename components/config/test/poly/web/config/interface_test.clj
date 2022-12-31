(ns poly.web.config.interface-test
  (:require
   [clojure.test :as test :refer :all]
   [poly.web.config.interface :as config]))

(deftest dev-profile
  (is (= {:debug true}
         (config/config "config/config.edn" {:profile :dev}))))

(deftest default-profile
  (is (= {:debug false}
         (config/config "config/config.edn" {:profile :default}))))
