(ns poly.web.user.store
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.logging.interface :as log]
   [poly.web.spec.interface :as spec]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.helpers :refer [from select where]]
   [poly.web.user.interface.spec :as user-spec]))

(defn table-to-ns
  "Converts SQL table names to their respective clojure namespaces."
  [^String table-name]
  (case table-name
    "users" "poly.web.user.interface.spec"
    ""))

(s/fdef find-by
  :args (s/cat :key keyword?
               :value any?)
  :ret (s/? map?))

(defn find-by
  [key value]
  (let [query (-> (select :*)
                  (from [:users])
                  (where [:= key value]))]
    (sql/query-one query {:qualifier-fn table-to-ns})))

(s/fdef find-by-email
  :args (s/cat :email ::user-spec/email)
  :ret (s/? map?))

(defn find-by-email
  [email]
  (log/info (str "looking for email" email))
  (find-by :email email))

(s/fdef find-by-username
  :args (s/cat :username spec/non-empty-string?)
  :ret (s/? map?))

(defn find-by-username
  [username]
  (find-by :username username))

(s/fdef find-by-id
  :args (s/cat :id uuid?)
  :ret (s/? map?))

(defn find-by-id
  [id]
  (find-by :id id))

(s/fdef insert-user!
  :args (s/cat :new-user ::user-spec/new-user)
  :ret ::user-spec/user)

(defn insert-user!
  [new-user]
  (sql/insert! :users new-user))
