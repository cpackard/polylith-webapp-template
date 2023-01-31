(ns poly.web.spec.interface
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.spec.core :as core]))

(def non-empty-string? core/non-empty-string?)

(def email? core/email?)

(def password? core/password?)

(s/def ::errors map?)
(def errors (s/keys :req-un [::errors]))
