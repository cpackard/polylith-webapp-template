(ns poly.web.auth.core
  (:require
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as time]
   [crypto.password.pbkdf2 :as crypto]
   [poly.web.logging.interface :as log]))

(defn generate-token
  [email username secret]
  (let [now   (time/now)
        claim {:sub username
               :iss email
               :exp (time/plus now (time/days 7))
               :iat now}]
    (jwt/sign claim secret)))

(defn token->claims
  [jwt secret]
  (try
    (jwt/unsign jwt secret)
    (catch Exception e
      (log/warn (.getMessage e))
      {:errors {:auth [(.getMessage e)]}})))

(defn encrypt-password
  [password]
  (-> password crypto/encrypt str))

(defn check
  "Compare a raw string with an encrypted string.
  Returns true if the string matches, false otherwise."
  [raw encrypted]
  (crypto/check raw encrypted))
