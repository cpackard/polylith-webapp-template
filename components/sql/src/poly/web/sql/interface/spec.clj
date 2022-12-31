(ns poly.web.sql.interface.spec
  "Public interface for spec definitions of the `sql` component."
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.sql.spec :as spec]))

(s/def ::connectable spec/connectable)

(s/def ::db-spec spec/db-spec)

(s/def ::migratus-config spec/migratus-config)
