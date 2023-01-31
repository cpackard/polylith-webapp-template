(ns poly.web.rest-api.spec
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::status (s/and pos-int? #(<= 100 % 600)))
(s/def ::body any?)
(s/def ::body-map (s/keys :req-un [::body]))
(s/def ::headers (s/with-gen (s/* (s/cat :header-name string?
                                         :header-val string?))
                   #(s/gen #{["Location" "/api/users/1"]})))
(s/def ::http-response (s/keys :req-un [::status
                                        ::body-map
                                        ::headers]))
