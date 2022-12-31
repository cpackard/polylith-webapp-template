(ns poly.web.sql.interface
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [poly.web.sql.core :as core]
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
  (core/create-pool db-spec))

(defmethod ig/halt-key! ::db-pool
  [_ datasource]
  (core/close-pool datasource))

(defn create-pool
  "Create a database connection pool given the `db-spec` config map."
  [db-spec]
  (core/create-pool db-spec))

(defn close-pool
  "Close the given database connection pool."
  [ds]
  [core/close-pool ds])

(defn query
  "Execute the given SQL query with optional arguments `opts`."
  [ds sql-query & opts]
  (apply core/query ds sql-query opts))

(defn query-one
  "Execute the given SQL query with optional arguments `opts`.

  Returns only the first result, if any."
  [ds sql-query & opts]
  (apply core/query-one ds sql-query opts))

(defn transaction
  "Execute each query sequentially in a transaction."
  [ds queries]
  (core/transaction ds queries))
