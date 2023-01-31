(ns poly.web.user.migrations.create-users-table
  "Migration for creating the `users` SQL table."
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.helpers :refer [create-table drop-table
                                           with-columns]]
   [poly.web.sql.interface.spec :as spec]))

(s/fdef migrate-up
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-up
  [config]
  (let [ds    (:db config)
        table (-> (create-table :users :if-not-exists)
                  (with-columns [[:id :uuid [:not nil] [:primary-key]]
                                 [:name [:varchar 255] [:constraint :users--name] :unique]]))]
    (sql/query table {} ds)))

(s/fdef migrate-down
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-down
  [config]
  (let [ds (:db config)]
    (sql/query (drop-table :users) {} ds)))

(comment
  (do
    ;(require '[poly.web.sql.interface :as sql])
    (sql/migrate!)))
