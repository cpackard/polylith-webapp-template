(ns poly.web.user.core
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.auth.interface :as auth]
   [poly.web.auth.interface.spec :as auth-s]
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
  [email password]
  (let [user        (store/find-by-email email)
        validations (some-fn validators/has-email?
                             (partial validators/password-match? password)
                             #(->> (::user-s/username %)
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

(defn process
  "Takes an input `data` and any number of single-argument functions.

  Calls each function with the result of the previous function
  (or `data` for the first function).
  Returns if the last function's result contains the `:errors` key,
  otherwise continues."
  [data & functions]
  (loop [d         data
         [f & fns] functions]
    (let [{:keys [errors] :as res} (f d)]
      (if (or errors (empty? fns))
        res
        (recur res fns)))))

(defn register!
  [{::user-s/keys [username email password] :as req-user}]
  (let [new-user    (assoc req-user
                           ::user-s/password
                           (auth/encrypt-password password))
        new-token (auth/generate-token email username)]
    (process new-user
             validators/existing-email?
             validators/existing-username?
             store/insert-user!
             #(user->visible-user % new-token))))
