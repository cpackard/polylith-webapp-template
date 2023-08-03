(ns poly.web.xtdb.core
  (:require [clojure.java.io :as io]
            [xtdb.api :as xt]))

;; (defn start-xtdb!
;;   [& {:keys [prefix] :or {prefix "data/dev"}}]
;;   (letfn [(kv-store [dir]
;;             {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
;;                         :db-dir (io/file dir)
;;                         :sync? true}})]
;;     (xt/start-node
;;      {:xtdb.jdbc/connection-pool {:dialect {:xtdb/module 'xtdb.jdbc.psql/->dialect}
;;                                   :db-spec {:dbtype   "postgres"
;;                                             :dbname   "core_db"
;;                                             :user "core_user"
;;                                             :password "core_pass_55%25"
;;                                             :host     "localhost"
;;                                             :port     5432}}
;;       :xtdb/tx-log {:xtdb/module 'xtdb.jdbc/->tx-log
;;                     :connection-pool :xtdb.jdbc/connection-pool}
;;       :xtdb/document-store {:xtdb/module 'xtdb.jdbc/->document-store
;;                             :connection-pool :xtdb.jdbc/connection-pool}
;;       :xtdb/index-store (kv-store (str prefix "/index-store"))})))

;; The following configuration uses an in-process RocksDB instance to write your data to disk. It will save your data in a directory named ./data/dev, relative to your project root. You do not need to create this directory. XTDB will create it for you.

(defn start-xtdb!
  [& {:keys [prefix] :or {prefix "data/dev/test"}}]
  (letfn [(kv-store [dir]
            {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
                        :db-dir (io/file dir)
                        :sync? true}})]
    (xt/start-node
     {:xtdb/tx-log (kv-store (str prefix "/tx-log"))
      :xtdb/document-store (kv-store (str prefix "/doc-store"))
      :xtdb/index-store (kv-store (str prefix "/index-store"))})))

;; note that attempting to eval this expression more than once before first calling `stop-xtdb!` will throw a RocksDB locking error
;; this is because a node that depends on native libraries must be `.close`'d explicitly
(comment
  (def xtdb-node (start-xtdb!)))

(comment
  ;; ingest
  (xt/submit-tx xtdb-node [[::xt/put
                            {:xt/id "hi2u"
                             :user/name "zig"}]])
  ;; query
  (xt/q (xt/db xtdb-node) '{:find [e]
                            :where [[e :user/name "zig"]]}))

(defn stop-xtdb! [xtdb-node]
  (.close xtdb-node))
