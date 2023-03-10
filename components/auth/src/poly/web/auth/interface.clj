(ns poly.web.auth.interface
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.auth.core :as core]
   [poly.web.auth.interface.spec :as auth-s]
   [poly.web.spec.interface :as spec]))

(s/fdef generate-token
  :args (s/cat :email spec/email?
               :username spec/non-empty-string?
               :secret ::auth-s/secret?)
  :ret ::auth-s/jwt-str?)

(defn generate-token
  "Generate an authentication token for the given user's email and username."
  [email username secret]
  (core/generate-token email username secret))

(s/fdef token->claims
  :args (s/cat :jwt ::auth-s/jwt-str?
               :secret ::auth-s/secret?)
  :ret (s/or :token ::auth-s/jwt
             :error ::spec/errors))

(defn token->claims
  "Return all claims for the given token (as a string)."
  [jwt secret]
  (core/token->claims jwt secret))

(s/fdef encrypt-password
  :args (s/cat :password spec/password?)
  :ret spec/non-empty-string?)

(defn encrypt-password
  "Encrypt the given password."
  [password]
  (core/encrypt-password password))

(s/fdef check
  :args (s/cat :raw spec/password?
               :encrypted spec/password?)
  :ret boolean?)

(defn check
  "Compare a raw string with an encrypted string.
  Returns true if the string matches, false otherwise."
  [raw encrypted]
  (core/check raw encrypted))
