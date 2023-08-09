(ns dev.workspace
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn read-workspace
  ([] (read-workspace "."))
  ([ws-dir]
   (-> ws-dir
       (io/file "workspace.edn")
       slurp
       edn/read-string)))

(defn require-components
  [& args]
  (let [flags (filter keyword? args)
        opts (interleave flags (repeat true))
        args (remove keyword? args)
        {:keys [top-namespace interface-ns]} (read-workspace)]
    (assert (and top-namespace interface-ns))
    (doseq [arg args
            :when arg
            :let [[alias & options] (if (vector? arg) arg [arg])
                  [component & libtail] (str/split (name alias) #"\.")
                  libparts (into [top-namespace component interface-ns] libtail)
                  lib (symbol (str/join "." libparts))
                  libspec (cond-> [lib]
                            (seq options) (into options)
                            (not-any? #{:as} options) (conj :as alias)
                            (seq opts) (into opts))]]
      (printf "(require '%s)\n" libspec) (flush)
      (require libspec))))

(defmacro reqcom [& args] `(apply require-components (quote ~args)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(comment

  ;; basic
  (reqcom user) ;; (require '[example.user.interface :as user])
  (reqcom account invoice) ;; (require '[example.acount.interface :as account]),
                           ;; (require '[example.invoice.interface :as invoice])

  ;; support flags & options
  (reqcom [user :as u])   ;; (require '[example.user.interface :as u])
  (reqcom user :reload)   ;; (require '[example.user.interface :as user :reload true])
  (reqcom sweet :refer-all) ;; (require '[example.sweet.interface :as sweet :refer-all true])

  ;; interface sub-namespaces
  (reqcom util.time) ;; (require '[example.util.interface.time :as util.time])
  (reqcom util.data)) ;; (require '[example.util.interface.data :as util.data])
