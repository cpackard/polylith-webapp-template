(ns poly.web.sql.interface-test
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :as test :refer [deftest is testing use-fixtures]]
   [poly.web.logging.interface.test-utils :as log-tu]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.helpers :refer [from select where]]
   [poly.web.sql.interface.test-utils :as sql-tu]
   [poly.web.sql.migratus :as sql-m]
   [poly.web.test-utils.interface :as tu]))

(def ^:private test-db-name "poly_web_sql_interface_test")

(def ^:private config
  "custom `migratus` config - overrides certain properties for testing."
  (let [table-name "test_app_migrations"
        directory (re-find #"components.*"
                           (str (io/resource "sql/test-migrations/")))]
    (-> sql-m/config
        (merge {:migration-table-name table-name :migration-dir directory})
        (assoc-in [:db :dbname] test-db-name))))

(use-fixtures :once
  (log-tu/set-log-config)
  (sql-tu/with-db! test-db-name)
  tu/pretty-spec!)

(use-fixtures :each
  (sql-tu/migration-cleanup! config))

(defn- table-exists?
  [table-name]
  (select [[:exists (-> (select)
                        (from [:information_schema.tables])
                        (where [:and
                                [:= :table_schema "public"]
                                [:= :table_name table-name]]))]]))

(deftest verify--migrations--create--migrate--rollback
  (testing "can create the migration files"
    (let [migration-ns (sql/create-migration! "create-users-table"
                                              'poly.web.sql.migrations
                                              config)]
      (is (= 1 (count (sql/pending-migrations config))))
      (-> migration-ns (str/replace #"-" "_") (str/replace #"\." "/") (str ".clj")
          io/resource
          io/delete-file)))

  (let [ds (:db config)]
    (testing "can run the generated migrations"
      (sql/migrate! config)
      (is (= true (:exists (sql/query-one (table-exists? "test_users") {} ds)))))

    (testing "can rollback the migrations"
      (sql/rollback! config)
      (is (= false (:exists (sql/query-one (table-exists? "test_users") {} ds)))))))
