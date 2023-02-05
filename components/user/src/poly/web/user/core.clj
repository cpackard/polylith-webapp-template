(ns poly.web.user.core
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.auth.interface :as auth]
   [poly.web.auth.interface.spec :as auth-s]
   [poly.web.macros.interface :as macros]
   [poly.web.user.interface.spec :as user-s]
   [poly.web.user.store :as store]
   [poly.web.user.validators :as validators]))

(s/def ::core-user (s/keys :req [::user-s/id
                                 ::user-s/name
                                 ::user-s/email
                                 ::user-s/password]
                           :opt [::user-s/username]))

(s/def ::user-with-pass (s/merge ::core-user
                                 (s/keys :req [::user-s/password])))

(s/def ::user-with-token (s/merge ::core-user
                                  (s/keys :req [::user-s/token])))

(s/fdef user->visible-user
  :args (s/cat :user ::user-with-pass
               :token ::auth-s/jwt-str?)
  :ret ::user-with-token
  :fn (fn [{:keys [args ret]}]
        (and (not (contains? ret ::user-s/password))
             (= (:token args) (::user-s/token ret)))))

(defn- user->visible-user
  [user token]
  (-> user
      (assoc ::user-s/token token)
      (dissoc ::user-s/password)))

(defn login
  [email password secret ds]
  (if-let [{::user-s/keys [email username] :as user} (store/find-by-email email ds)]
    (macros/some-fn-> :errors
                      user
                      (validators/password-match? password)
                      (user->visible-user (auth/generate-token email username secret)))
    {:errors {:email ["Invalid email."]}}))

(defn user-by-token
  [token secret ds]
  (if-let [user (-> (auth/token->claims token secret)
                    :sub
                    (store/find-by-username ds))]
    (user->visible-user user token)
    {:errors {:token ["Cannot find a user with associated token."]}}))

(defn register!
  [{::user-s/keys [email username password] :as req-user} secret ds]
  (let [new-user    (assoc req-user
                           ::user-s/password
                           (auth/encrypt-password password))
        new-token (auth/generate-token email username secret)]
    (macros/some-fn-> :errors
                      new-user
                      (validators/existing-email? ds)
                      (validators/existing-username? ds)
                      (store/insert-user! ds)
                      (user->visible-user new-token))))
