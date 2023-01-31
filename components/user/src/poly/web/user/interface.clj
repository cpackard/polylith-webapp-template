(ns poly.web.user.interface
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.auth.interface.spec :as auth-s]
   [poly.web.spec.interface :as spec]
   [poly.web.user.core :as core]
   [poly.web.user.interface.spec :as user-s]))

(def base-user (s/keys :req [::user-s/name
                             ::user-s/email]
                       :opt [::user-s/username]))

(s/def ::new-user (s/merge base-user
                           (s/keys :req [::user-s/username ::user-s/password])))

(s/def ::visible-user (s/merge base-user
                               (s/keys :req [::user-s/token
                                             ::user-s/id])))

(def user-response (s/cat :val (s/alt :errors ::spec/errors
                                      :response ::visible-user)))

(s/fdef user-by-token
  :args (s/cat :token ::user-s/token :secret spec/non-empty-string?)
  :ret ::visible-user
  :fn (fn [{:keys [args ret]}]
        (= (::user-s/token args) (::user-s/token ret))))

(defn user-by-token
  "Retrieve the user associated with the given token."
  [token secret]
  (core/user-by-token token secret))

(s/fdef login
  :args (s/cat :email ::user-s/email
               :password ::user-s/password
               :secret spec/non-empty-string?)
  :ret user-response)

(defn login
  "Login as an existing user."
  [email password secret]
  (core/login email password secret))

(s/fdef register!
  :args (s/cat :user ::new-user :secret ::auth-s/secret?)
  :ret user-response)

(defn register!
  "Create a new user."
  [new-user secret]
  (core/register! new-user secret))
