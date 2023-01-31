(ns poly.web.rest-api.interface
  (:require
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [poly.web.logging.interface :as log]
   [poly.web.rest-api.core :as core]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.spec :as sql-s]))

(defmethod ig/init-key :poly.web.logging.interface/log-config
  [_ config]
  (log/merge-config! config))

(defmethod ig/init-key ::env
  [_ env]
  env)

(defmethod ig/init-key ::service-map
  [_ {:keys [pool env extras reloadable?]}]
  (core/service-map pool env extras reloadable?))

(defmethod ig/init-key ::server
  [_ {:keys [service-map]}]
  (core/start service-map))

(defmethod ig/halt-key! ::server
  [_ server]
  (core/stop server))

(defmethod ig/pre-init-spec :poly.web.sql.interface/db-spec
  [_]
  sql-s/db-spec)

(defmethod ig/pre-init-spec :poly.web.sql.interface/db-pool
  [_]
  (s/keys :req-un [::sql-s/db-spec]))

(defmethod ig/init-key :poly.web.sql.interface/db-spec
  [_ db-spec]
  db-spec)

(defmethod ig/init-key :poly.web.sql.interface/db-pool
  [_ {:keys [db-spec]}]
  (sql/init-pool db-spec))

(defmethod ig/halt-key! :poly.web.sql.interface/db-pool
  [_ pool]
  (sql/close-pool pool))
