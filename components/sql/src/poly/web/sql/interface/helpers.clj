(ns poly.web.sql.interface.helpers
  (:require
   [honey.sql.helpers :as helpers]))

(defn create-table
  [& args]
  (apply helpers/create-table args))

(defn drop-table
  [& tables]
  (apply helpers/drop-table tables))

(defn with-columns
  [& args]
  (apply helpers/with-columns args))
