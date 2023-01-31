(ns poly.web.auth.core
  (:require
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as time]
   [clojure.spec.alpha :as s]
   [crypto.password.pbkdf2 :as crypto]
   [poly.web.config.interface :as config]
   [poly.web.logging.interface :as log]))

(s/fdef token-secret
  :args (s/cat)
  :ret string?)

(defn- token-secret
  "Retrieve the secret value used to sign and unsign tokens."
  []
  (if-let [secret-val (config/env :secret)]
    secret-val
    "default-secret-value-probably-should-change"))

(defn generate-token
  [email username]
  (let [now   (time/now)
        claim {:sub username
               :iss email
               :exp (time/plus now (time/days 7))
               :iat now}]
    (jwt/sign claim (token-secret))))

(defn token->claims
  [jwt]
  (try
    (jwt/unsign jwt (token-secret))
    (catch Exception e
      (log/warn (.getMessage e))
      true)))

(defn encrypt-password
  [password]
  (-> password crypto/encrypt str))

(defn check
  "Compare a raw string with an encrypted string.
  Returns true if the string matches, false otherwise."
  [raw encrypted]
  (crypto/check raw encrypted))
