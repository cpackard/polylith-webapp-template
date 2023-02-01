(ns poly.web.sql.migrations
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [migratus.core :as migratus]
   [poly.web.config.interface :as cfg]))

(def ^:private migration-template
  "
(ns %s
  \"Migration file to create the `users `table.\"
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.spec :as spec]
   [poly.web.sql.interface.helpers :refer [create-table drop-table with-columns]]))

(s/fdef migrate-up
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-up
  [config]
  (let [ds    (:db config)
        table (-> (create-table :test_users :if-not-exists)
                  (with-columns [[:id :uuid [:not nil] [:primary-key]]
                                 [:name [:varchar 255] [:constraint :users--name] :unique]]))]
    (sql/query table ds)))

(s/fdef migrate-down
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-down
  [config]
  (let [ds (:db config)]
    (sql/query (drop-table :test_users) ds)))

(comment
  (do
    ;(require '[poly.web.sql.interface :as sql])
    (sql/migrate!)))
")

(def ^:private edn-template
  "
{:ns %s
 :up-fn migrate-up
 :down-fn migrate-down
 :transaction? %s}
")

(def config
  {:store                :database
   :migration-dir        "components/sql/resources/sql/migrations/"
   :migration-table-name "app_migrations"
   :db                   (-> (cfg/parse (io/resource "rest-api/config.edn"))
                             :poly.web.sql.interface/db-spec)})

(defn- project-file-path
  "Get the relative path from the project root -> migration namespace's file."
  [ns component]
  (let [ns-str (-> (string/replace ns #"-" "_")
                   (string/replace #"\." "/"))]
    (format "components/%s/src/%s.clj" component ns-str)))

(defn- write-file!
  "Create a new migration file."
  [full-ns parent-ns]
  (let [component (-> (string/split parent-ns #"\.") reverse second)
        template  (format migration-template full-ns)]
    (spit (project-file-path full-ns component) (string/trim template))))

(defn migration-files
  "Return a seq of all migration files."
  [& {:as config}]
  (->> (io/file (:migration-dir (merge config config)))
       (file-seq)
       (filter (fn [f] (string/ends-with? (str f) ".edn")))))

(defn create-migration!
  "Create a new migration file at the given namespace."
  [name parent-ns tx? ds-opts]
  (-> (merge config ds-opts)
      (migratus/create name :edn))
  (let [file (->> (migration-files ds-opts)
                  (filter #(string/includes? % name))
                  first
                  str)
        full-ns (str parent-ns "." name)
        edn (format (string/trim edn-template) full-ns tx?)]
    (spit file edn)
    (write-file! full-ns (str parent-ns))
    full-ns))

(defn migrate!
  [opts]
  (migratus/migrate (merge config opts)))

(defn rollback!
  [migration-id opts]
  (let [config (merge config opts)]
    (if migration-id
      (migratus/rollback-until-just-after config migration-id)
      (migratus/rollback config))))

(defn reset-migrations!
  [opts]
  (migratus/reset (merge config opts)))

(defn pending-migrations
  [opts]
  (migratus/pending-list (merge config opts)))
