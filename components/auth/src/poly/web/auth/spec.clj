(ns poly.web.auth.spec
  (:require
   [clojure.spec.alpha :as s]))

(def ^:private base-64-re "[a-zA-Z0-9/+-_]+=*")
(def ^:private jwt-re (re-pattern (apply format "%s\\.%s\\.%s" (repeat 3 base-64-re))))

(def ^:private sample-jwt-strs #{"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwIiwiaXNzIjoiandvdzgyNEB5YWhvby5jb20iLCJleHAiOjE2NzMzMDI3MDcsImlhdCI6MTY3MjY5NzkwN30.nR7ILJrHy37GTlOqbB168-Nzt_FShWRMCLJ9jVuXbFw"
                                 "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaXNzIjoiandvdzgyNEB5YWhvby5jb20iLCJleHAiOjE2NzMzMDI0MjEsImlhdCI6MTY3MjY5NzYyMX0.UUldu7iCG5Crkci0FNqD-ZrQIjCI6-1ITMkQIY1NkMg"})

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
