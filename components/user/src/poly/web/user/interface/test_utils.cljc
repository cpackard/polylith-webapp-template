(ns poly.web.user.interface.test-utils
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [poly.web.user.interface :as user]
   [poly.web.user.interface.spec :as user-s]))

(s/def user-keyword? #{::user-s/id
                       ::user-s/name
                       ::user-s/email
                       ::user-s/username
                       ::user-s/password})

(s/def ::visible-user (s/keys :req [::user-s/id
                                    ::user-s/name
                                    ::user-s/email
                                    ::user-s/token]))

(s/fdef new-user!
  :args (s/cat :kwarg-pairs (s/* (s/cat :keyword ::user-keyword?
                                        :val any?))
               :user-map (s/? (s/keys :opt [::user-s/id
                                            ::user-s/name
                                            ::user-s/email
                                            ::user-s/username
                                            ::user-s/password])))
  :ret ::visible-user)

(defn new-user!
  "Generate a new user and register with the DB for testing"
  [& {::user-s/keys [id name email username password]
      :as new-user}]
  (let [new-user (-> (merge (gen/generate (s/gen ::user/new-user))
                            new-user)
                     user/register!)]
    (if-let [errors (:errors new-user)]
      (throw (ex-info "could not create new user" errors))
      new-user)))
