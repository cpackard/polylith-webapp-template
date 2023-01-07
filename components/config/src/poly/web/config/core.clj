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

(def ^:private system
  "Reference to the Integrant system dependencies (initialized on app startup)."
  (atom {}))

(defn env
  [key]
  (get @system key))

(defn init
  [cfg]
  (reset! system (ig/init cfg)))

(defn halt!
  ([]
   (halt! @system))
  ([sys]
   (when (identical? @system sys)
     (reset! system nil))
   (ig/halt! sys)))
