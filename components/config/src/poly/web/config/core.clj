(ns poly.web.config.core
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [integrant.core :as ig]))

;; Allows aero lib to parse the #ig/ref tag.
(defmethod aero/reader 'ig/ref
  [{:keys [profile] :as opts} tag value]
  (integrant.core/ref value))

;; TODO: refactor so the caller must pass in `(io/resource ...)`
(defn config
  [cfg opts]
  (aero/read-config (io/resource cfg) opts))

(defn init
  [cfg] ; TODO: add spec
  (ig/init cfg))
