(ns poly.web.sql.interface-test
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.test :as test :refer [deftest is use-fixtures]]
   [honey.sql.helpers :refer [from select where]]
   [migratus.core :as migratus]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.migratus :as sql-m]))

(def config (merge sql-m/config
                   {:migration-dir (re-find #"components.*" (str (io/resource "sql/test-migrations/")))}))

(defn migration-files
  "Return a seq of all migration files."
  []
  (->> (io/file (:migration-dir config))
       (file-seq)
       (filter (fn [f] (string/ends-with? (str f) ".edn")))))

(defn prepare-for-tests
  [f]
  (let [migration-cleanup (fn []
                            (doseq [file (migration-files)]
                              (when (string/ends-with? file ".edn")
                                (io/delete-file file true))))]
    (migration-cleanup)
    (f)
    (migration-cleanup)))

(use-fixtures :each prepare-for-tests)

(defn table-exists?
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
    (is (= true (:exists (sql/query-one (table-exists? "users") ds))))

    (migratus/rollback config)
    (is (= false (:exists (sql/query-one (table-exists? "users") ds))))))
