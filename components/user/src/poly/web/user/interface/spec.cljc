(ns poly.web.user.interface.spec
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.auth.interface.spec :as auth-spec]
   [poly.web.spec.interface :as spec]))

(s/def ::id uuid?)
(s/def ::name spec/non-empty-string?)
(s/def ::password spec/password?)
(s/def ::email spec/email?)
(s/def ::username (s/and spec/non-empty-string?
                         (fn [s] (< (count s) 30)))) ;; TODO: how can this val come from the schema definition?
(s/def ::token auth-spec/jwt-str?)

(s/def ::core-user (s/keys :req [::name
                                 ::email]
                           :opt [::username]))

(s/def ::new-user (s/merge ::core-user
                           (s/keys :req [::username ::password])))

(s/def ::user (s/merge ::core-user
                       (s/keys :req [::id ::password])))

(s/def ::visible-user (s/merge ::core-user
                               (s/keys :req [::id ::token])))
