(ns poly.web.user.migrations.add--user--email--username--password
  "Migration file to add the `email`, `username`, and `password` columns
  to the `users` table."
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.helpers :refer [add-column alter-table create-index
                                           drop-column]]
   [poly.web.sql.interface.spec :as spec]))

(s/fdef migrate-up
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-up
  [config]
  (let [ds (:db config)
        add-columns    (alter-table :users
                                    (add-column :password [:varchar 200] [:not nil])
                                    (add-column :email
                                                [:varchar 255]
                                                [:not nil]
                                                [:constraint :users--email--unique]
                                                :unique)
                                    (add-column :username [:varchar 30]))
        email-index    (create-index :users--email--index :users :email)
        username-index (create-index :users--username--index :users :username)]
    (doseq [query [add-columns email-index username-index]]
      (sql/query query {} ds))))

(s/fdef migrate-down
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-down
  [config]
  (let [ds             (:db config)
        drop-columns   (-> (alter-table :users
                                        (drop-column :password)
                                        (drop-column :email)
                                        (drop-column :username)))]
    (sql/query drop-columns {} ds)))

(comment
  (do
    (require '[poly.web.sql.migratus :as sqlm])
    (require '[migratus.core :as migratus])
    (migratus/migrate sqlm/config)))
