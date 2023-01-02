(ns poly.web.auth.interface
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.auth.core :as core]
   [poly.web.auth.interface.spec :as auth-spec]
   [poly.web.spec.interface :as spec]))

(s/fdef generate-token
  :args (s/cat :email spec/email?
               :username spec/non-empty-string?)
  :ret ::auth-spec/jwt-str?)

(defn generate-token
  "Generate an authentication token for the given user's email and username."
  [email username]
  (core/generate-token email username))

(s/fdef token->claims
  :args (s/cat :jwt ::auth-spec/jwt-str?)
  :ret ::auth-spec/jwt)

(defn token->claims
  "Return all claims for the given token (as a string)."
  [jwt]
  (core/token->claims jwt))

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
