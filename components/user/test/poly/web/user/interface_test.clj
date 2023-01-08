(ns poly.web.user.interface-test
  "Tests for the component-level functionality of the `user` brick
  exposed via `poly.web.user.interface`."
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test :as test :refer [deftest is testing use-fixtures]]
   [expound.alpha :as expound]
   [migratus.core :as migratus]
   [poly.web.config.interface :as config]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.migratus :as sql-m]
   [poly.web.user.interface :as user]
   [poly.web.user.interface.spec :as user-spec]))

(defn- configs
  "Parse all configs requested in `cfgs` into a single merged map."
  [& cfgs]
  (let [read-fn (fn [cfg] (config/config cfg {:profile :dev}))]
    (->> (map read-fn cfgs)
         (apply merge))))

(defn- prep-expound-and-database
  "Enable pretty-printing for spec errors and setup/teardown test DB."
  [db-name f]
  (set! s/*explain-out* expound/printer)

  (let [ds        (-> (configs "sql/config.edn"
                               "auth/config.edn")
                      ::sql/db-spec)
        opts      {}
        create-db {:raw (format "CREATE DATABASE %s" db-name)}
        drop-db   {:raw (format "DROP DATABASE IF EXISTS %s WITH (FORCE)" db-name)}]
    (sql/query create-db opts ds)

    (f)

    (sql/query drop-db opts ds)))

(defn- reset-migrations
  "Reset any DB migrations between test runs."
  [db-name f]
  (let [test-sys     (->  (configs  "sql/config.edn"
                                    "auth/config.edn")
                          (assoc-in [::sql/db-spec :dbname] db-name)
                          config/init)
        migratus-cfg (assoc-in sql-m/config [:db :dbname] db-name)]

    (migratus/migrate migratus-cfg)

    (f)

    (config/halt! test-sys)))

(let [test-db-name "poly_web_user_interface_test"]
  (use-fixtures
    :once (partial prep-expound-and-database test-db-name)
    :each (partial reset-migrations test-db-name)))

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
  (testing "unregistered user cannot login"
    (let [email    (gen/generate (s/gen ::user-spec/email))
          password (gen/generate (s/gen ::user-spec/password))]
      (is (= {:errors {:email ["Invalid email."]}}
             (user/login email password)))))
  (let [user (gen/generate (s/gen ::user-spec/new-user))
        email (::user-spec/email user)
        password (::user-spec/password user)]
    (user/register! user)
    (testing "existing user cannot login with incorrect password"
      (is (= {:errors {:password ["Invalid password."]}}
             (user/login email "bad-password"))))
    (testing "existing user can login with correct password"
      (let [visible-user (user/login email password)]
        (is (= email (::user-spec/email visible-user))
            (format "Expected email %s from %s" email visible-user))
        (is (some? (::user-spec/token visible-user))
            (format "Expected new token with user %s" visible-user))))))
