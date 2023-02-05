(ns poly.web.user.test-utils
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [poly.web.config.interface :as cfg]
   [poly.web.spec.test-utils :as spec-tu]
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
  [ds & {:as new-user}]
  (let [requ (-> (gen/generate (s/gen ::user/new-user))
                 (merge  {::user-s/email (spec-tu/gen-email)} new-user))
        newu (user/register! requ cfg/default-secret-value ds)]
    (if-let [errors (:errors newu)]
      (throw (ex-info "could not create new user" errors))
      (merge newu (select-keys requ [::user-s/password])))))
