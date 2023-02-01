(ns poly.web.user.store
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.logging.interface :as log]
   [poly.web.spec.interface :as spec]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.helpers :refer [from select where]]
   [poly.web.sql.interface.spec :as sql-s]
   [poly.web.user.interface.spec :as user-s]))

(defn table-to-ns
  "Converts SQL table names to their respective clojure namespaces."
  [^String table-name]
  (case table-name
    "users" "poly.web.user.interface.spec"
    ""))

(s/fdef find-by
  :args (s/cat :key keyword?
               :value any?
               :ds ::sql-s/connectable)
  :ret (s/? map?))

(defn find-by
  [key value ds]
  (let [query (-> (select :*)
                  (from [:users])
                  (where [:= key value]))]
    (sql/query-one query ds {:qualifier-fn table-to-ns})))

(s/fdef find-by-email
  :args (s/cat :email ::user-s/email :ds ::sql-s/connectable)
  :ret (s/? map?))

(defn find-by-email
  [email ds]
  (log/debug (str "looking for email" email))
  (find-by :email email ds))

(s/fdef find-by-username
  :args (s/cat :username spec/non-empty-string? :ds ::sql-s/connectable)
  :ret (s/? map?))

(defn find-by-username
  [username ds]
  (find-by :username username ds))

(s/fdef find-by-id
  :args (s/cat :id ::user-s/id :ds ::sql-s/connectable)
  :ret (s/? map?))

(defn find-by-id
  [id ds]
  (find-by :id id ds))

(s/def ::new-user (s/keys :req [::user-s/username
                                ::user-s/name
                                ::user-s/email
                                ::user-s/password]))

(s/def ::registered-user (s/merge ::new-user
                                  (s/keys :req [::user-s/id])))

(s/fdef insert-user!
  :args (s/cat :new-user ::new-user :ds ::sql-s/connectable)
  :ret ::registered-user)

(defn insert-user!
  [new-user ds]
  (sql/insert! new-user :users ds {:qualifier-fn table-to-ns}))
