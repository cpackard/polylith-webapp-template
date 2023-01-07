(ns poly.web.user.validators
  (:require
   [poly.web.auth.interface :as auth]
   [poly.web.user.interface.spec :as user-spec]
   [poly.web.user.store :as store]))

(defn- format-err
  [k msg]
  {:errors {k [msg]}})

(defn has-email?
  [{::user-spec/keys [email]}]
  (when-not (store/find-by-email email)
    (format-err :email "Invalid email.")))

(defn password-match?
  [password user]
  (when-not (auth/check password (::user-spec/password user))
    (format-err :password "Invalid password.")))

(defn existing-email?
  [{::user-spec/keys [email]}]
  (when (store/find-by-email email)
    (format-err :email "A user exists with the given email.")))

(defn existing-username?
  [{::user-spec/keys [username]}]
  (when (store/find-by-username username)
    (format-err :username "A user exists with the given username.")))

(defn user-created?
  [user]
  (when (nil? (store/insert-user! user))
    (format-err :other "Cannot insert user into db.")))
