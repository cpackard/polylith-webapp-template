(ns poly.web.sql.interface-test
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.test :as test :refer [deftest is use-fixtures]]
   [honey.sql.helpers :refer [from select where]]
   [migratus.core :as migratus]
   [poly.web.sql.interface :as sql]
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

(defn- migration-files
  "Return a seq of all migration files."
  []
  (->> (io/file (:migration-dir config))
       (file-seq)
       (filter (fn [f] (string/ends-with? (str f) ".edn")))))

(defn- migration-cleanup!
  "Delete the SQL migration table
  and all `.edn` files in the migration directory."
  []
  (let [migration-table (keyword (:migration-table-name config))
        ds              (:db config)]
    (sql/query {:drop-table [:if-exists migration-table]} {} ds))
  (doseq [file (migration-files)]
    (when (string/ends-with? file ".edn")
      (io/delete-file file true))))

(defn- prepare-for-tests
  [f]
  (migration-cleanup!)
  (f)
  (migration-cleanup!))

(let [cfgs         ["sql/config.edn"]
      opts         {:profile :dev}
      log-cfg {:min-level [[#{"poly.web.sql.*"} :info]]}]
  (use-fixtures :once
    (tu/set-log-config log-cfg)
    tu/pretty-spec!
    (tu/with-db! test-db-name (tu/sys-cfg cfgs opts)))
  (use-fixtures :each prepare-for-tests))

(defn- table-exists?
  [table-name]
  (select [[:exists (-> (select)
                        (from [:information_schema.tables])
                        (where [:and
                                [:= :table_schema "public"]
                                [:= :table_name table-name]]))]]))

(deftest verify--migrations--create--migrate--rollback
  ; create a migration file
  (migratus/create config "create-users-table" :edn)
  ; find the migration file
  (let [migration-file (str (first (migration-files)))
        ;; NOTE: the `:ns` and `:-fn` keys are only quoted here
        ;; because this is written in a `.clj` file.
        ;; Quotes are *not* needed for migrations
        ;; written directly in their generated `.edn` files.
        create-users-cfg '{:ns poly.web.sql.migrations.create-users-table
                           :up-fn migrate-up
                           :down-fn migrate-down
                           :transaction? true}
        ds (:db config)]
    ; write contents of the migration config to the migration file
    (spit migration-file (with-out-str (pr create-users-cfg)))
    (is (= 1 (count (migratus/pending-list config))))

    (migratus/migrate config)
    (is (= true (:exists (sql/query-one (table-exists? "test_users") {} ds))))

    (migratus/rollback config)
    (is (= false (:exists (sql/query-one (table-exists? "test_users") {} ds))))))
