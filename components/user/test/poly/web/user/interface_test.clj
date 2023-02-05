(ns poly.web.user.interface-test
  "Tests for the component-level functionality of the `user` brick
  exposed via `poly.web.user.interface`."
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :as test :refer [deftest is use-fixtures]]
   [poly.web.auth.interface.spec :as auth-s]
   [poly.web.config.interface :as cfg]
   [poly.web.logging.test-utils :as log-tu]
   [poly.web.spec.test-utils :as spec-tu]
   [poly.web.sql.test-utils :as sql-tu]
   [poly.web.test-utils.interface :as test-utils]
   [poly.web.user.interface :as user]
   [poly.web.user.interface.spec :as user-s]
   [poly.web.user.test-utils :as user-tu]))

(defn test-resources
  []
  {:secret cfg/default-secret-value
   :ds     (sql-tu/ds)})

(let [test-db-name "poly_web_user_interface_test"
      log-cfg {:min-level
               [[#{"com.zaxxer.*" "migratus.*" "io.pedestal.*"} :error]
                [#{"poly.web.user.*"} :info]]}]
  (use-fixtures :once
    (log-tu/set-log-config log-cfg)
    (sql-tu/with-db! test-db-name)
    test-utils/pretty-spec!)
  (use-fixtures :each
    (sql-tu/reset-migrations! test-db-name)))

(deftest register!
  (let [users   (map first (s/exercise ::user/new-user))
        secret  (-> ::auth-s/secret? s/gen gen/generate)
        results (map (fn [user]
                       (user/register! user secret (sql-tu/ds)))
                     users)]
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

(deftest user-by-token--invalid-tokens
  (let [{:keys [secret ds]} (test-resources)

        token (gen/generate (s/gen ::user-s/token))
        res   (user/user-by-token token secret ds)]
    (is (=  {:errors {:token ["Cannot find a user with associated token."]}}
            res))))

(deftest user-by-token--success
  (let [{:keys [secret ds]} (test-resources)

        email (spec-tu/gen-email)
        user  (user-tu/new-user! (sql-tu/ds) ::user-s/email email)
        res   (user/user-by-token (::user-s/token user) secret ds)]
    (is (= (dissoc user ::user-s/password)
           res))))

(comment
  (deftest user-by-token--all
    (user-by-token--invalid-tokens)
    (user-by-token--success)))

(deftest login--unregistered-user
  (let [{:keys [secret ds]} (test-resources)

        email    (gen/generate (s/gen ::user-s/email))
        password (gen/generate (s/gen ::user-s/password))]
    (is (= {:errors {:email ["Invalid email."]}}
           (user/login email password secret ds)))))

(deftest login--registered-user-incorrect-password
  (let [{:keys [secret ds]} (test-resources)

        user     (gen/generate (s/gen ::user/new-user))
        email    (::user-s/email user)]
    (user/register! user secret ds)
    (is (= {:errors {:password ["Invalid password."]}}
           (user/login email "bad-password" secret ds)))))

(deftest login--success
  (let [{:keys [secret ds]} (test-resources)

        {::user-s/keys [email password]
         :as           user}
        (gen/generate (s/gen ::user/new-user))]
    (user/register! user secret ds)

    (let [visible-user (user/login email password secret ds)]
      (is (= email (::user-s/email visible-user))
          (format "Expected email %s from %s" email visible-user))
      (is (some? (::user-s/token visible-user))
          (format "Expected new token with user %s" visible-user)))))

(comment
  (deftest login--all
    (login--unregistered-user)
    (login--registered-user-incorrect-password)
    (login--success)))
