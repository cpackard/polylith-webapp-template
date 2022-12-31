(ns poly.web.sql.migrations.create-users-table
  "Example namespace for a database migration.

  Used in this component's tests."
  (:require
   [clojure.spec.alpha :as s]
   [honey.sql.helpers :refer [create-table drop-table with-columns]]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.spec :as spec]))

(s/fdef migrate-up
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-up
  [config]
  (let [ds    (:db config)
        table (-> (create-table :users :if-not-exists)
                  (with-columns [[:id :uuid [:not nil] [:primary-key]]
                                 [:name [:varchar 255] [:constraint :users--name] :unique]]))]
    (sql/query ds table)))

(s/fdef migrate-down
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-down
  [config]
  (let [ds (:db config)]
    (sql/query ds (drop-table :users))))
