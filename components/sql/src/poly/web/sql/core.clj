(ns poly.web.sql.core
  "Implementation of functionality for the `sql` component."
  (:require
   [honey.sql :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.sql :as jdbc-sql])
  (:import
   (com.zaxxer.hikari HikariDataSource)))


(defn init-pool
  [db-spec]
  (let [ds (connection/->pool HikariDataSource db-spec)]
    (jdbc/with-options ds {:label-fn (:label-fn jdbc/snake-kebab-opts)})))

(defn close-pool
  "Close the database pool `datasource`."
  [datasource]
  (.close (:connectable datasource)))

(defn query
  "Run the given SQL query string and query params"
  [sql-query ds & opts]
  (jdbc-sql/query ds
                  (sql/format sql-query)
                  (merge {:timeout 2}
                         opts)))

(defn query-one
  "Same as the `query` function, but only returns the first matching row."
  [sql-query ds & opts]
  (first (apply query sql-query ds opts)))

(defn transaction
  [ds queries]
  (jdbc/with-transaction [tx ds]
    (doseq [query queries]
      (jdbc/execute! tx (sql/format query)))))
