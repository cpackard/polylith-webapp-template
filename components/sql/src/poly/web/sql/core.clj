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
  :args (s/cat :ds spec/connectable
               :query map?))

(defn query
  "Run the given SQL query string and query params"
  [ds sql-query & opts]
  (jdbc-sql/query ds
                  (sql/format sql-query)
                  (merge {:timeout 2
                          :qualifier-fn (fn [_] str "projects")}
                         opts)))

(s/fdef query-one
  :args (s/cat :ds spec/connectable
               :query map?)
  :ret (complement sequential?))

(defn query-one
  "Same as the `query` function, but only returns the first matching row."
  [ds sql-query & opts]
  (first (apply query ds sql-query opts)))

(s/fdef transaction
  :args (s/cat :ds ::spec/connectable
               :queries (s/+ map?)))

(defn transaction
  [ds queries]
  (jdbc/with-transaction [tx ds]
    (doseq [query queries]
      (jdbc/execute! tx (sql/format query)))))
