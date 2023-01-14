(ns poly.web.user.interface
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [poly.web.spec.interface :as spec]
   [poly.web.user.core :as core]
   [poly.web.user.interface.spec :as user-s]))

(defmethod ig/pre-init-spec ::secret
  [_]
  string?)

(defmethod ig/init-key ::secret
  [_ secret]
  secret)

(def base-user (s/keys :req [::user-s/id
                             ::user-s/name
                             ::user-s/email]
                       :opt [::user-s/username]))

(s/def ::new-user (s/merge base-user
                           (s/keys :req [::user-s/username ::user-s/password])))

(s/def ::visible-user (s/merge base-user
                               (s/keys :req [::user-s/token])))

(def user-response (s/cat :val (s/alt :errors ::spec/errors
                                      :response ::visible-user)))

(s/fdef user-by-token
  :args (s/cat :token ::user-s/token)
  :ret ::visible-user
  :fn (fn [{:keys [args ret]}]
        (= (::user-s/token args) (::user-s/token ret))))

(defn user-by-token
  "Retrieve the user associated with the given token."
  [token]
  (core/user-by-token token))

(s/fdef login
  :args (s/cat :email ::user-s/email
               :password ::user-s/password)
  :ret user-response)

(defn login
  "Login as an existing user."
  [email password]
  (core/login email password))

(s/fdef register!
  :args (s/cat :user ::new-user)
  :ret user-response)

(defn register!
  "Create a new user."
  [new-user]
  (core/register! new-user))
