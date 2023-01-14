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
  [{::user-s/keys [email]}]
  (when-not (store/find-by-email email)
    (format-err :email "Invalid email.")))

(defn password-match?
  [password user]
  (when-not (auth/check password (::user-s/password user))
    (format-err :password "Invalid password.")))

(defn existing-email?
  [{::user-s/keys [email]}]
  (when (store/find-by-email email)
    (format-err :email "A user exists with the given email.")))

(defn existing-username?
  [{::user-s/keys [username]}]
  (when (store/find-by-username username)
    (format-err :username "A user exists with the given username.")))

(defn user-created?
  [user]
  (when (nil? (store/insert-user! user))
    (format-err :other "Cannot insert user into db.")))
