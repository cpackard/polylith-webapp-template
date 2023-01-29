(ns poly.web.sql.interface.helpers
  (:require
   [honey.sql :as h]
   [honey.sql.helpers :as helpers]
   [poly.web.sql.core :as core]))

;; allows honey.sql to understand `core/create-index`
(h/register-clause! :create-index
                    (fn [clause x]
                      (let [[index-name [table column]] x]
                        [(format "%s %s ON %s(%s)"
                                 (h/sql-kw clause)
                                 (h/format-entity index-name)
                                 (h/format-entity table)
                                 (h/format-entity column))]))

                    :add-column)

(defn create-table
  [& args]
  (apply helpers/create-table args))

(defn alter-table
  [& args]
  (apply helpers/alter-table args))

(defn drop-table
  [& tables]
  (apply helpers/drop-table tables))

(defn create-index
  [index-name table column]
  (core/create-index index-name table column))

(defn drop-index
  [index]
  (helpers/drop-index index))

(defn add-index
  [& args]
  (apply helpers/add-index args))

(defn add-column
  [& args]
  (apply helpers/add-column args))

(defn drop-column
  [& args]
  (apply helpers/drop-column args))

(defn with-columns
  [& args]
  (apply helpers/with-columns args))

(defn select
  [& exprs]
  (apply helpers/select exprs))

(defn from
  [& tables]
  (apply helpers/from tables))

(defn where
  [& exprs]
  (apply helpers/where exprs))
