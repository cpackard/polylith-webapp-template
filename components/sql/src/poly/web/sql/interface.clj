(ns poly.web.sql.interface
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [poly.web.spec.interface :as spec]
   [poly.web.sql.core :as core]
   [poly.web.sql.interface.spec :as sql-s]
   [poly.web.sql.migrations :as migrations]))

(defmethod ig/pre-init-spec ::db-spec
  [_]
  sql-s/db-spec)

(defmethod ig/pre-init-spec ::db-pool
  [_]
  (s/keys :req-un [::sql-s/db-spec]))

(defmethod ig/init-key ::db-spec
  [_ db-spec]
  db-spec)

(defmethod ig/init-key ::db-pool
  [_ {:keys [db-spec]}]
  (core/init-pool db-spec))

(defmethod ig/halt-key! ::db-pool
  [_ pool]
  (core/close-pool pool))

(s/fdef init-pool
  :args (s/cat :db-spec ::sql-s/db-spec))

(defn init-pool
  "Create a database connection pool given the `db-spec` config map."
  [db-spec]
  (core/init-pool db-spec))

(s/fdef close-pool
  :args (s/cat :pool ::sql-s/connectable))

(defn close-pool
  "Close the given database connection pool."
  [pool]
  (core/close-pool pool))

(s/fdef query
  :args (s/cat :query map?
               :opts (s/? map?)
               :ds (s/? sql-s/connectable)))

(defn query
  "Execute the given SQL query with optional arguments `opts`."
  ([query]
   (core/query query {} (core/ds)))
  ([query opts]
   (core/query query opts (core/ds)))
  ([query opts ds]
   (core/query query opts ds)))

(s/fdef query-one
  :args (s/cat :query map?
               :opts (s/? map?)
               :ds (s/? sql-s/connectable))
  :ret (complement sequential?))

(defn query-one
  "Execute the given SQL query with optional arguments `opts`.

  Returns only the first result, if any."
  ([query]
   (core/query-one query {} (core/ds)))
  ([query opts]
   (core/query-one query opts (core/ds)))
  ([query opts ds]
   (core/query-one query opts ds)))

(s/fdef insert!
  :args (s/cat :table keyword?
               :row map?
               :ds (s/? ::sql-s/connectable))
  :ret (s/nilable map?))

(defn insert!
  ([table row]
   (insert! table row (core/ds)))
  ([table row ds]
   (core/insert! table row ds)))

(s/fdef transaction
  :args (s/cat :ds ::sql-s/connectable
               :queries (s/+ map?)))

(defn transaction
  "Execute each query sequentially in a transaction."
  [ds queries]
  (core/transaction ds queries))

(s/def ::migration-file? spec/non-empty-string?)
(s/def ::ns? spec/non-empty-string?)
(s/def ::up-fn? spec/non-empty-string?)
(s/def ::down-fn? spec/non-empty-string?)
(s/def ::transaction? boolean?)

(s/def ::migration-kw? #{::ns ::up-fn? ::down-fn? ::transaction?})

(s/def ::migration (s/keys :opt-un [::ns ::up-fn? ::down-fn? ::transaction?]))

(s/fdef create-migration-file
  :args (s/cat :name ::migration-file?
               :ns ::ns?
               :tx? ::transaction?
               :migration-cfg (s/cat :kwarg-pairs (s/* (s/cat :keyword ::migration-kw?
                                                              :val any?))
                                     :opts-map (s/? ::migration))))

(defn create-migration!
  "Create a timestamped file for a new DB migration.

  TODO: expand docstring with kwarg options."
  [name ns & {:keys [tx?] :or {tx? true} :as opts}]
  (migrations/create-migration! name ns tx? opts))

(defn migrate!
  "Run any outstanding migrations"
  [& {:as opts}]
  (migrations/migrate! opts))

(defn rollback!
  "Rollback all migrations after `migration-id`. Defaults to the last migration.

  This only considers completed migrations, and will not migrate up."
  [& {:keys [migration-id] :as opts}]
  (migrations/rollback! migration-id opts))

(defn reset-migrations!
  "Reset the database by running `down` on any migrations successfully applied
  and then running `up` for all migrations."
  [& {:as opts}]
  (migrations/reset-migrations! opts))

(defn pending-migrations
  "Return a list of all pending migrations."
  [& {:as opts}]
  (migrations/pending-migrations opts))
