(ns poly.web.user.migrations.user-id-to-int
  "Migration file to create the `users `table."
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.helpers :refer [alter-column alter-table]]
   [poly.web.sql.interface.spec :as spec]))

(s/fdef migrate-up
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-up
  [config]
  (let [ds           (:db config)
        new-seq      {:raw "CREATE SEQUENCE users__id__seq;"}
        drop-default (-> (alter-table :users)
                         (alter-column :id :drop :default))
        new-type     (-> (alter-table :users)
                         (alter-column :id
                                       :set :data :type :integer
                                       :using [:nextval "users__id__seq"]))
        default-val  (-> (alter-table :users)
                         (alter-column :id
                                       :set [:default [:nextval "users__id__seq"]]))
        type->seq    {:raw "ALTER SEQUENCE users__id__seq OWNED BY users.id;"}]
    (sql/transaction ds [new-seq drop-default new-type default-val type->seq])))

(s/fdef migrate-down
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-down
  [config]
  (let [ds (:db config)
        drop-default (-> (alter-table :users)
                         (alter-column :id :drop :default))
        rollback-users (-> (alter-table :users)
                           (alter-column :id
                                         :set :data :type :uuid
                                         [:using [:gen_random_uuid]]))
        reset-default (-> (alter-table :users)
                          (alter-column :id
                                        :set :default
                                        [:gen_random_uuid]))
        drop-seq {:raw "DROP SEQUENCE users__id__seq CASCADE"}]
    (sql/transaction ds [drop-default rollback-users reset-default drop-seq])))

(comment
  (do
    ;(require '[poly.web.sql.interface :as sql])
    (sql/migrate!)))
