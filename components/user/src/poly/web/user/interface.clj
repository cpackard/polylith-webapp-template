(ns poly.web.user.interface
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [poly.web.auth.interface.spec :as auth-spec]
   [poly.web.spec.interface :as spec]
   [poly.web.user.core :as core]
   [poly.web.user.interface.spec :as user-spec]))

(defmethod ig/pre-init-spec ::secret
  [_]
  string?)

(defmethod ig/init-key ::secret
  [_ secret]
  secret)

(def user-response (s/cat :val (s/alt :errors ::spec/errors
                                      :response ::user-spec/visible-user)))

(s/fdef user-by-token
  :args (s/cat :token ::auth-spec/jwt-str?)
  :ret ::user-spec/visible-user
  :fn (fn [{:keys [args ret]}]
        (= (::user-spec/token args) (::user-spec/token ret))))

(defn user-by-token
  "Retrieve the user associated with the given token."
  [token]
  (core/user-by-token token))

(s/fdef login
  :args (s/cat :email ::user-spec/email
               :password ::user-spec/password)
  :ret user-response)

(defn login
  "Login as an existing user."
  [email password]
  (core/login email password))

(s/fdef register!
  :args (s/cat :user ::user-spec/new-user)
  :ret user-response)

(defn register!
  "Create a new user."
  [new-user]
  (core/register! new-user))
