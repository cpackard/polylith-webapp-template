(ns poly.web.sql.core
  "Implementation of functionality for the `sql` component."
  (:require
   [clojure.spec.alpha :as s]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.sql :as jdbc-sql]
   [poly.web.sql.interface.spec :as sql-spec]
   [poly.web.sql.spec :as spec])
  (:import
   (com.zaxxer.hikari HikariDataSource)))

(s/fdef create-pool
  :args (s/cat :db-spec ::sql-spec/db-spec))

(defn create-pool
  [db-spec]
  (let [ds (connection/->pool HikariDataSource db-spec)]
    (jdbc/with-options ds {:label-fn     (:label-fn jdbc/snake-kebab-opts)})))

(s/fdef close-pool
  :args (s/cat :datasource spec/connectable))

(defn close-pool
  "Close the database pool `datasource`."
  [datasource]
  (.close (:connectable datasource)))

(s/fdef query
  :args (s/cat :query map?
               :ds spec/connectable))

(defn query
  "Run the given SQL query string and query params"
  [sql-query ds & opts]
  (jdbc-sql/query ds
                  (sql/format sql-query)
                  (merge {:timeout 2}
                         opts)))

(s/fdef query-one
  :args (s/cat :query map?
               :ds spec/connectable)
  :ret (complement sequential?))

(defn query-one
  "Same as the `query` function, but only returns the first matching row."
  [sql-query ds & opts]
  (first (apply query sql-query ds opts)))

(s/fdef transaction
  :args (s/cat :ds ::spec/connectable
               :queries (s/+ map?)))

(defn transaction
  [ds queries]
  (jdbc/with-transaction [tx ds]
    (doseq [query queries]
      (jdbc/execute! tx (sql/format query)))))
