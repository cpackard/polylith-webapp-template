(ns poly.web.auth.interface.spec
  (:require
   [clojure.spec.alpha :as s]))

(def ^:private base-64-re "[a-zA-Z0-9/+-_]+=*")
(def ^:private jwt-re (re-pattern (apply format "%s\\.%s\\.%s" (repeat 3 base-64-re))))

; TODO: figure out a better way to auto-generate these values (tokens expire after 7 days)
(def ^:private sample-jwt-strs #{"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYW1wbGUyIiwiaXNzIjoic2FtcGxlMkBleGFtcGxlLmNvbSIsImV4cCI6MTY3NDA2NzA1OCwiaWF0IjoxNjczNDYyMjU4fQ.EVSs5DNO2qXphjkhLd65tpAbjZJwmC-u9DH8IyUZO-o"})

(def jwt-str? (s/with-gen (s/and string? #(re-matches jwt-re %))
                #(s/gen sample-jwt-strs)))

(s/def ::sub string?)
(s/def ::iss string?)
(s/def ::exp pos-int?)
(s/def ::iat pos-int?)

(def jwt (s/keys :req-un [::sub
                          ::iss
                          ::exp
                          ::iat]))

(s/def ::jwt jwt)

(s/def ::jwt-str? jwt-str?)
