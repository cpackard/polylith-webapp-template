(ns poly.web.config.interface
  (:require
   [poly.web.config.core :as core]))

; (aero/read-config (io/resource "config.edn") profile)

(defn config
  ([cfg]
   (config cfg {:profile :default}))
  ([cfg opts]
   (core/config cfg opts)))

; TODO: should specs be defined for `interface`, `core`, or both?

(defn init
  [cfg]
  (core/init cfg))
