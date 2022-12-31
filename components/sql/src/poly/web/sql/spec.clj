(ns poly.web.sql.spec
  "Spec definitions directly for the `sql` component."
  (:require
   [clojure.spec.alpha :as s]
   [next.jdbc.protocols :as p]))

(def connectable (s/or :connectable #(satisfies? p/Connectable %)
                       :sourceable #(satisfies? p/Sourceable %)))

(def db-spec (s/keys :req-un [::dbtype ::dbname]
                     :opt-un [::classname
                              ::user ::password
                              ::host ::port
                              ::dbname-separator
                              ::host-prefix]))

(s/def ::store keyword?)
(s/def ::migration-dir string?)
(s/def ::migration-table-name string?)
(s/def ::db db-spec)

(def migratus-config (s/keys :req-un [::store
                                      ::migration-dir
                                      ::migration-table-name
                                      ::db]))
