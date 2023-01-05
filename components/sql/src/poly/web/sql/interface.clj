(ns poly.web.sql.interface
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [poly.web.sql.core :as core]
   [poly.web.sql.interface.spec :as sql-spec]
   [poly.web.sql.spec :as spec]))

(defmethod ig/pre-init-spec ::db-spec
  [_]
  spec/db-spec)

(defmethod ig/pre-init-spec ::db-pool
  [_]
  (s/keys :req-un [::db-spec spec/db-spec]))

(defmethod ig/init-key ::db-spec
  [_ db-spec]
  db-spec)

(defmethod ig/init-key ::db-pool
  [_ {:keys [db-spec]}]
  (core/init-pool db-spec))

(defmethod ig/halt-key! ::db-pool
  [_ _]
  (core/close-pool))

(s/fdef init-pool
  :args (s/cat :db-spec ::sql-spec/db-spec))

(defn init-pool
  "Create a database connection pool given the `db-spec` config map."
  [db-spec]
  (core/init-pool db-spec))

(defn close-pool
  "Close the given database connection pool."
  []
  (core/close-pool))

(s/fdef query
  :args (s/cat :query map?
               :ds (s/? spec/connectable)
               :opts (s/? map?)))

(defn query
  "Execute the given SQL query with optional arguments `opts`."
  ([query]
   (core/query query (core/ds) {}))
  ([query ds]
   (core/query query ds {}))
  ([query ds opts]
   (core/query query ds opts)))

(s/fdef query-one
  :args (s/cat :query map?
               :ds (s/? spec/connectable)
               :opts (s/? map?))
  :ret (complement sequential?))

(defn query-one
  "Execute the given SQL query with optional arguments `opts`.

  Returns only the first result, if any."
  ([query]
   (core/query-one query (core/ds) {}))
  ([query ds]
   (core/query-one query ds {}))
  ([query ds opts]
   (core/query-one query ds opts)))

(s/fdef insert!
  :args (s/cat :table keyword?
               :row map?
               :ds (s/? ::spec/connectable))
  :ret map?)

(defn insert!
  ([table row]
   (insert! table row (core/ds)))
  ([table row ds]
   (core/insert! table row ds)))

(s/fdef transaction
  :args (s/cat :ds ::spec/connectable
               :queries (s/+ map?)))

(defn transaction
  "Execute each query sequentially in a transaction."
  [ds queries]
  (core/transaction ds queries))
