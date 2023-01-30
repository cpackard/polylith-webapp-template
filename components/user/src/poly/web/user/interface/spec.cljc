(ns poly.web.user.interface.spec
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.auth.interface.spec :as auth-s]
   [poly.web.spec.interface :as spec]))

(s/def ::id pos-int?)
(s/def ::name spec/non-empty-string?)
(s/def ::password spec/password?)
(s/def ::email spec/email?)
(s/def ::username (s/and spec/non-empty-string?
                         (fn [s] (< (count s) 30)))) ;; TODO: how can this val come from the schema definition?
(s/def ::token auth-s/jwt-str?)
