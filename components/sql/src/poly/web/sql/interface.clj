(ns poly.web.sql.interface
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.spec.interface :as spec]
   [poly.web.sql.core :as core]
   [poly.web.sql.interface.spec :as sql-s]
   [poly.web.sql.migrations :as migrations]))

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
               :ds ::sql-s/connectable
               :opts (s/? map?)))

(defn query
  "Execute the given SQL query with optional arguments `opts`."
  [query ds & {:as opts}]
  (core/query query ds opts))

(s/fdef query-one
  :args (s/cat :query map?
               :ds ::sql-s/connectable
               :opts (s/? map?))
  :ret (complement sequential?))

(defn query-one
  "Execute the given SQL query with optional arguments `opts`.

  Returns only the first result, if any."
  [query ds & {:as opts}]
  (core/query-one query ds opts))

(s/fdef insert!
  :args (s/cat :row map?
               :table keyword?
               :ds ::sql-s/connectable
               :opts (s/? map?))
  :ret (s/nilable map?))

(defn insert!
  [row table ds & {:as opts}]
  (core/insert! row table ds opts))

(s/fdef transaction
  :args (s/cat :queries (s/+ map?)
               :ds ::sql-s/connectable
               :opts (s/? map?)))

(defn transaction
  "Execute each query sequentially in a transaction."
  [queries ds & {:as opts}]
  (core/transaction queries ds opts))

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
