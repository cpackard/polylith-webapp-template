(ns poly.web.sql.core
  "Implementation of functionality for the `sql` component."
  (:require
   [honey.sql :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.sql :as jdbc-sql])
  (:import
   (com.zaxxer.hikari HikariDataSource)))

(def ^:private db-pool
  "Private reference to the DB connection pool."
  (atom nil))

(defn ds
  "Helper function to access the DB connection pool"
  []
  @db-pool)

(defn init-pool
  [db-spec]
  (when (some? @db-pool)
    @db-pool)
  (let [ds (connection/->pool HikariDataSource db-spec)]
    (reset! db-pool
            (jdbc/with-options ds {:label-fn (:label-fn jdbc/snake-kebab-opts)}))))

(defn close-pool
  "Close the database pool `datasource`."
  []
  (.close (:connectable @db-pool))
  (reset! db-pool nil))

(defn query
  "Run the given SQL query string and query params"
  [sql-query ds opts]
  (jdbc-sql/query ds
                  (sql/format sql-query)
                  (merge {:timeout 2}
                         opts)))

(defn query-one
  "Same as the `query` function, but only returns the first matching row."
  [sql-query ds opts]
  (first (query sql-query ds opts)))

(defn transaction
  [ds queries]
  (jdbc/with-transaction [tx ds]
    (doseq [query queries]
      (jdbc/execute! tx (sql/format query)))))

(defn create-index
  "Wrapper function to help with constructing CREATE INDEX queries."
  [index-name table column]
  {:create-index [index-name [table column]]})
