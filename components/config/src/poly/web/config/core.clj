(ns poly.web.config.core
  (:require
   [aero.core :as aero]
   [integrant.core :as ig]))

;; Allows aero lib to parse the #ig/ref tag.
(defmethod aero/reader 'ig/ref
  [_ _ value]
  (integrant.core/ref value))

(defn parse
  [cfg opts]
  (aero/read-config cfg opts))

(defn parse-cfgs
  "Parse all configs in `cfgs` into a single merged map."
  [cfgs opts]
  (reduce merge (map #(parse % opts)
                     cfgs)))

(defn init
  [cfg]
  (ig/init cfg))

(defn halt!
  [sys]
  (ig/halt! sys))
