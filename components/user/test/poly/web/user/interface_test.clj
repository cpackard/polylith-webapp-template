(ns poly.web.user.interface-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :as test :refer [deftest is testing use-fixtures]]
   [expound.alpha :as expound]
   [migratus.core :as migratus]
   [poly.web.config.interface :as config]
   [poly.web.sql.migratus :as sql-m]
   [poly.web.user.interface :as user]
   [poly.web.user.interface.spec :as user-spec]))

(defn prep-expound-and-system-for-tests
  [f]
  (let [system (->> (map (fn [comp-cfg]
                           (config/config comp-cfg {:profile :dev}))
                         ["sql/config.edn"
                          "auth/config.edn"])
                    (apply merge)
                    (config/init))]
    (set! s/*explain-out* expound/printer)
    (migratus/reset sql-m/config)

    (f)

    (migratus/reset sql-m/config)
    (config/halt! system)))

(use-fixtures :once prep-expound-and-system-for-tests)

(deftest register!
  (let [register-tests (s/exercise-fn `user/register!)
        results (map second register-tests)]
    (is (some #{{:errors {:email ["A user exists with the given email."]}}
                {:errors {:username ["A user exists with the given username."]}}
                {:errors {:other ["Cannot insert user into db."]}}}
              results)
        "Expected at least one auto-generated to collide and trigger an error.")
    (is (not-empty (filter #(s/valid? ::user-spec/visible-user %)
                           results))
        "Expected at least one user to be created successfully.")))

(deftest user-by-token
  (testing "invalid tokens do not return a user"
    (is (=  {:errors {:token ["Cannot find a user with associated token."]}}
            (user/user-by-token (gen/generate (s/gen ::user-spec/token))))))
  (testing "valid tokens return their user"
    (let [user (->  (gen/generate (s/gen ::user-spec/new-user))
                    (user/register!))]
      (is (= user (user/user-by-token (::user-spec/token user)))))))

(deftest login
  (testing "unregistered user"
    (let [email    (gen/generate (s/gen ::user-spec/email))
          password (gen/generate (s/gen ::user-spec/password))]
      (is (= {:errors {:email ["Invalid email."]}}
             (user/login email password)))))
  (let [user (gen/generate (s/gen ::user-spec/new-user))
        email (::user-spec/email user)
        password (::user-spec/password user)]
    (user/register! user)
    (testing "password mismatch"
      (is (= {:errors {:password ["Invalid password."]}}
             (user/login email "bad-password"))))
    (testing "valid credentials can login"
      (let [visible-user (user/login email password)]
        (is (= email (::user-spec/email visible-user))
            (format "Expected email %s from %s" email visible-user))
        (is (some? (::user-spec/token visible-user))
            (format "Expected new token with user %s" visible-user))))))
