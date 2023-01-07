(ns poly.web.user.core
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.auth.interface :as auth]
   [poly.web.auth.interface.spec :as auth-spec]
   [poly.web.user.interface.spec :as user-spec]
   [poly.web.user.store :as store]
   [poly.web.user.validators :as validators]))

(s/fdef user->visible-user
  :args (s/cat :user ::user-spec/user
               :token ::auth-spec/jwt-str?)
  :ret ::user-spec/visible-user
  :fn (fn [{:keys [args ret]}]
        (and (not (contains? ret ::user-spec/password))
             (= (:token args) (::user-spec/token ret)))))

(defn user->visible-user
  [user token]
  (-> user
      (assoc ::user-spec/token token)
      (dissoc ::user-spec/password)))

(defn login
  [email password]
  (let [user        (store/find-by-email email)
        validations (some-fn validators/has-email?
                             (partial validators/password-match? password)
                             #(->> (::user-spec/username %)
                                   (auth/generate-token email)
                                   (user->visible-user user)))]
    (validations user)))

(defn user-by-token
  [token]
  (if-let [user (-> (auth/token->claims token)
                    :sub
                    store/find-by-username)]
    (user->visible-user user token)
    {:errors {:token ["Cannot find a user with associated token."]}}))

(defn register!
  [{::user-spec/keys [username email password] :as req-user}]
  (let [new-user   (assoc req-user
                          ::user-spec/password (auth/encrypt-password password)
                          ::user-spec/id (java.util.UUID/randomUUID))
        new-token  (auth/generate-token email username)
        validatons (some-fn validators/existing-email?
                            validators/existing-username?
                            validators/user-created?
                            #(user->visible-user % new-token))]
    (validatons new-user)))
