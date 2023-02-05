(ns poly.web.auth.interface.spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [poly.web.auth.core :as core]
   [poly.web.config.interface :as cfg]
   [poly.web.spec.interface :as spec]))

(def ^:private base-64-re "[a-zA-Z0-9/+-_]+=*")
(def ^:private jwt-re (re-pattern (apply format "%s\\.%s\\.%s" (repeat 3 base-64-re))))

(def jwt-str? (s/with-gen (s/and string? #(re-matches jwt-re %))
                #(gen/fmap (fn [[email username secret]]
                             (core/generate-token email username secret))
                           (gen/tuple (s/gen spec/email?)
                                      (s/gen spec/str-min-len-20?)
                                      (s/gen #{cfg/default-secret-value})))))

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

(s/def ::secret? spec/str-min-len-20?)
