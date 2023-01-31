(ns poly.web.spec.test-utils
  (:require
   [clojure.spec.gen.alpha :as gen]))

(def email (gen/fmap #(str % "@test.com")
                     (gen/such-that #(not= % "")
                                    (gen/string-alphanumeric))))

(defn gen-email
  []
  (gen/generate email))
