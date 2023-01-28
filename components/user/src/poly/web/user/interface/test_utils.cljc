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
  [& {:as new-user}]
  (let [newu (-> (gen/generate (s/gen ::user/new-user))
                 (merge new-user)
                 user/register!)]
    (if-let [errors (:errors newu)]
      (throw (ex-info "could not create new user" errors))
      newu)))
