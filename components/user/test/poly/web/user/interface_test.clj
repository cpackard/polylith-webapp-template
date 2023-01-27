(ns poly.web.user.interface-test
  "Tests for the component-level functionality of the `user` brick
  exposed via `poly.web.user.interface`."
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :as test :refer [deftest is testing use-fixtures]]
   [poly.web.logging.interface.test-utils :as log-tu]
   [poly.web.spec.interface.test-utils :as spec-tu]
   [poly.web.sql.interface.test-utils :as sql-tu]
   [poly.web.test-utils.interface :as test-utils]
   [poly.web.user.interface :as user]
   [poly.web.user.interface.spec :as user-s]
   [poly.web.user.interface.test-utils :as user-tu]))

(let [test-db-name "poly_web_user_interface_test"
      log-cfg {:min-level [[#{"poly.web.user.*"} :info]]}]
  (use-fixtures :once
    (log-tu/set-log-config log-cfg)
    (sql-tu/with-db! test-db-name)
    test-utils/pretty-spec!)
  (use-fixtures :each
    (sql-tu/reset-migrations! test-db-name)))

(deftest register!
  (let [register-tests (s/exercise-fn `user/register!)
        results (map second register-tests)]
    (is (some #{{:errors {:email ["A user exists with the given email."]}}
                {:errors {:username ["A user exists with the given username."]}}
                {:errors {:other ["Cannot insert user into db."]}}}
              results)
        "Expected at least one auto-generated to collide and trigger an error.")
    (is (empty? (filter #(get-in % [:errors :request])
                        results))
        "Expected only valid users to be generated.")
    (is (not-empty (filter #(s/valid? ::user/visible-user %)
                           results))
        "Expected at least one user to be created successfully.")))

(deftest user-by-token
  (testing "invalid tokens do not return a user"
    (is (=  {:errors {:token ["Cannot find a user with associated token."]}}
            (user/user-by-token (gen/generate (s/gen ::user-s/token))))))
  (testing "valid tokens return their user"
    (let [email (spec-tu/gen-email)
          user (user-tu/new-user! ::user-s/email email)]
      (is (= user (user/user-by-token (::user-s/token user)))))))

(deftest login
  (testing "unregistered user cannot login"
    (let [email    (gen/generate (s/gen ::user-s/email))
          password (gen/generate (s/gen ::user-s/password))]
      (is (= {:errors {:email ["Invalid email."]}}
             (user/login email password)))))

  (let [user (gen/generate (s/gen ::user/new-user))
        email (::user-s/email user)
        password (::user-s/password user)]
    (user/register! user)
    (testing "existing user cannot login with incorrect password"
      (is (= {:errors {:password ["Invalid password."]}}
             (user/login email "bad-password"))))
    (testing "existing user can login with correct password"
      (let [visible-user (user/login email password)]
        (is (= email (::user-s/email visible-user))
            (format "Expected email %s from %s" email visible-user))
        (is (some? (::user-s/token visible-user))
            (format "Expected new token with user %s" visible-user))))))
