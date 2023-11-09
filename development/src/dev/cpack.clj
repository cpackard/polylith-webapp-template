#_{:clj-kondo/ignore [:unused-namespace :unused-referred-var]}
(ns dev.cpack
  (:require
   [poly.web.gpg.interface :as gpg]
   [poly.web.sql.interface :as sql]
   [poly.web.config.interface :as cfg]
   [poly.web.rest-api.interface :as api-base]
   [java-time.api :as jt]
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [integrant.core :as ig]
   [honey.sql :as hsql]
   [honey.sql.helpers :as hsh]
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as st]
   [clojure.string :as str]
   [expound.alpha :as expound]
   [integrant.repl :refer [go halt reset]]
   [integrant.repl.state :refer [system]]
   [clojure.data.json :as json]))

(defn str->spec
  "Converts a postgresql connection string to a spec map."
  [db-url]
  (let [creds-str              (second (str/split db-url #"://"))
        [username rem-str-one] (str/split creds-str #":")
        [password rem-str-two] (str/split rem-str-one #"@")
        [host     db-name]     (str/split rem-str-two #"/")]
    {:dbtype   "postgres"
     :dbname   db-name
     :username username
     :password password
     :host     host
     :port     5432}))

(defn load-db-file
  "Load the database connection file."
  []
  (println "Enter GPG Passphrase:")
  (let [passphrase       (read-line)    ; Test123!
        gpg-info         (-> "dev/cpack-gpg.edn" io/resource io/as-file slurp edn/read-string)
        pubkey-file      (-> (:pubkey gpg-info) io/resource io/as-file)
        privkey-file     (-> (:privkey gpg-info) io/resource io/as-file)
        pubkey           (gpg/load-pubkey pubkey-file)
        privkey          (gpg/load-privkey pubkey passphrase privkey-file)
        filename         ".db-connections.gpg"
        db-file-contents (-> (slurp filename) (gpg/decrypt privkey) str/split-lines)]
    (into {} (map (fn [kw jdbc-url] [kw jdbc-url])
                  [:local :dev :prod-ro :prod-WRITE]
                  db-file-contents))))

(defn load-db-spec
  "Load the database spec for the requested env.

  If no argument is provided, the local database will be loaded.

  Accepted keywords are :local, :dev, :prod-ro, and :prod-WRITE."
  ([]
   (load-db-spec :local))
  ([db-kw]
   (let [db-url (db-kw (load-db-file))]
     (->> db-url
          (str->spec)))))

;; define a zero-argument function that returns a prepped Integrant configuration.
(integrant.repl/set-prep!
  ;; TODO: create a map of {::sql/pool } to the returned db spec from load-db-file
 (fn []
    ;; NOTE: change db-key to the desired env, then call `(go)` or `(reset)` to start the system
   (let [db-key  :prod-ro
         db-spec (load-db-spec db-key)]
     (-> [(io/resource "dev/cpack.edn")]
         (cfg/parse-cfgs {:profile :local})
         (merge {:poly.web.sql.interface/db-spec db-spec})))))

(defn get-ds
  "Retrieve the initialized connection pool from Integrant."
  []
  (::sql/db-pool system))

(defn setup
  []
    ;; Human-readable spec errors
    ;; see the docs for details: https://github.com/bhb/expound/blob/master/doc/faq.md#how-do-i-use-expound-to-print-all-spec-errors
  (set! s/*explain-out* expound/printer)

    ;; Enable instrumentation for all registered `spec`s
  (st/unstrument)
  (st/instrument)

    ;; start Integrant system
  (go))

(comment
  ;; NOTE: run this block to load the required components
  ;; Make sure that the REPL has been loaded with the aliases below!
  ;; -M:dev:test:repl/reloaded:cider/nrepl
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (do
    (require '[dev.workspace :as ws])
    (ws/reqcom gpg sql))

  ;; NOTE: now eval this buffer, then you can run the rest of the snippets
  (go)
  (sql/query {:raw "SELECT COUNT(*) FROM core_user"} (get-ds))

  (halt))
