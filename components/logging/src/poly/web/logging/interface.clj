(ns poly.web.logging.interface
  (:require
   [integrant.core :as ig]
   [poly.web.logging.core :as core]))

(defmacro debug
  [& args]
  `(core/debug ~@args))

(defmacro info
  [& args]
  `(core/info ~@args))

(defmacro warn
  [& args]
  `(core/warn ~@args))

(defmacro error
  [& args]
  `(core/error ~@args))

(defmacro with-config
  [config & body]
  `(core/with-config ~config ~@body))

(defmacro with-merged-config
  [config & body]
  `(core/with-merged-config ~config ~@body))

(defmacro merge-config!
  [config]
  `(core/merge-config! ~config))

(defmethod ig/init-key ::log-config [_ config]
  (merge-config! config))
