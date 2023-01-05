(ns poly.web.logging.interface
  (:require
   [poly.web.logging.core :as core]))

(defmacro info
  [& args]
  `(core/info ~@args))

(defmacro warn
  [& args]
  `(core/warn ~@args))

(defmacro error
  [& args]
  `(core/error ~@args))
