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
  [_ datasource]
  (core/close-pool datasource))

(s/fdef init-pool
  :args (s/cat :db-spec ::sql-spec/db-spec))

(defn init-pool
  "Create a database connection pool given the `db-spec` config map."
  [db-spec]
  (core/init-pool db-spec))

(s/fdef close-pool
  :args (s/cat :datasource spec/connectable))

(defn close-pool
  "Close the given database connection pool."
  [ds]
  [core/close-pool ds])

(s/fdef query
  :args (s/cat :query map?
               :ds spec/connectable))

(defn query
  "Execute the given SQL query with optional arguments `opts`."
  [ds sql-query & opts]
  (apply core/query ds sql-query opts))

(s/fdef query-one
  :args (s/cat :query map?
               :ds spec/connectable)
  :ret (complement sequential?))

(defn query-one
  "Execute the given SQL query with optional arguments `opts`.

  Returns only the first result, if any."
  [ds sql-query & opts]
  (apply core/query-one ds sql-query opts))

(s/fdef transaction
  :args (s/cat :ds ::spec/connectable
               :queries (s/+ map?)))

(defn transaction
  "Execute each query sequentially in a transaction."
  [ds queries]
  (core/transaction ds queries))
