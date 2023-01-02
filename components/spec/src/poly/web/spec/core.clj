(ns poly.web.spec.core
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]))

(def ^:private email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(def non-empty-string? (s/and string?
                              #(not (str/blank? %))))

(def email? (s/with-gen (s/and string? #(re-matches email-regex %))
              #(s/gen #{"user-1@example.com" "jwow824@yahoo.com" "aubp@ca.gov"})))

(def password? (s/and string?
                      #(>= (count %) 8)))
