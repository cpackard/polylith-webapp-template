(ns poly.web.logging.core
  (:require
   [taoensso.timbre :as timbre]))

(defmacro debug
  [& args]
  `(timbre/debug ~@args))

(defmacro info
  [& args]
  `(timbre/info ~@args))

(defmacro warn
  [& args]
  `(timbre/warn ~@args))

(defmacro error
  [& args]
  `(timbre/error ~@args))

(defmacro with-config
  [config & body]
  `(timbre/with-config ~config ~@body))

(defmacro with-merged-config
  [config & body]
  `(timbre/with-merged-config ~config ~@body))

(defmacro merge-config!
  [config]
  `(timbre/merge-config! ~config))
