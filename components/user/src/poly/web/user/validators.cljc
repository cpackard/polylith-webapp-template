(ns poly.web.user.validators
  (:require
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [poly.web.auth.interface :as auth]
   [poly.web.user.interface.spec :as user-s]
   [poly.web.user.store :as store]))

(defn- format-err
  [k & msgs]
  {:errors {k (apply vector msgs)}})

(defn valid-user?
  [spec-kw user]
  (when-not (s/valid? spec-kw user)
    (format-err :request
                "Invalid request."
                (expound/expound-str spec-kw user))))

(defn has-email?
  [{::user-s/keys [email] :as user} ds]
  (if (store/find-by-email email ds)
    (format-err :email "Invalid email.")
    user))

(defn password-match?
  [{::user-s/keys [password] :as user} req-password]
  (if-not (auth/check req-password password)
    (format-err :password "Invalid password.")
    user))

(defn existing-email?
  [{::user-s/keys [email] :as user} ds]
  (if (store/find-by-email email ds)
    (format-err :email "A user exists with the given email.")
    user))

(defn existing-username?
  [{::user-s/keys [username] :as user} ds]
  (if (store/find-by-username username ds)
    (format-err :username "A user exists with the given username.")
    user))
