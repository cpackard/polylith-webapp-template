(ns poly.web.logging.interface
  (:require
   [poly.web.logging.core :as core]))

(defmacro info
  [msg]
  `(core/info ~msg))

(defmacro warn
  [msg]
  `(core/warn ~msg))

(defmacro error
  [msg]
  `(core/error ~msg))
