(ns poly.web.sql.core
  "Implementation of functionality for the `sql` component."
  (:require
   [honey.sql :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as jdbc-sql]
   [poly.web.logging.interface :as log])
  (:import
   (com.zaxxer.hikari HikariDataSource)
   (java.sql ResultSet ResultSetMetaData)))

(defn default-column-reader
  "Reads the raw response from SQL and optionally performs conversions."
  [^ResultSet rs ^ResultSetMetaData rsmeta ^Integer i]
  (try
    (let [ct (.getColumnTypeName rsmeta i)
          row (.getObject rs i)]
      (cond (nil? row) row
            ;; (= "date" ct) (jt/local-date row)
            ;; (= "timestamptz" ct) (.toInstant row)
            :else row))
    (catch Exception e
      (log/info (.getMessage e)))))

(defn init-pool
  [db-spec]
  (let [opts     (merge jdbc/snake-kebab-opts
                        {:builder-fn (rs/as-maps-adapter rs/as-modified-maps
                                                         default-column-reader)})
        new-pool (-> (connection/->pool HikariDataSource db-spec)
                     (jdbc/with-options opts))]
    ;; this code initializes the pool and performs a validation check:
    (.close (jdbc/get-connection new-pool))
    new-pool))

(defn close-pool
  "Close the database pool `datasource`."
  [pool]
  (.close (:connectable pool)))

(defn query
  "Run the given SQL query string and query params"
  [sql-query ds & {:as opts}]
  (jdbc-sql/query ds
                  (sql/format sql-query)
                  (merge {:timeout 2} opts)))

(defn query-one
  "Same as the `query` function, but only returns the first matching row."
  [sql-query ds & {:as opts}]
  (first (query sql-query ds opts)))

(defn insert!
  [row table ds & {:as opts}]
  (try
    (jdbc-sql/insert! ds table row opts)
    (catch Exception e
      (log/warn "Unable to add row to db."
                :row row
                :table table
                :exc-msg (.getMessage e))
      {:errors {:sql [(.getMessage e)]}})))

(defn transaction
  [queries ds & {:as opts}]
  (jdbc/with-transaction [tx ds opts]
    (doseq [query queries]
      (jdbc/execute! tx (sql/format query)))))

(defn create-index
  "Wrapper function to help with constructing CREATE INDEX queries."
  [index-name table column]
  {:create-index [index-name [table column]]})
