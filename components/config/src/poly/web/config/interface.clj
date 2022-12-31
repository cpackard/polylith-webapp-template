(ns poly.web.config.interface
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.config.core :as core]
   [poly.web.config.spec :as spec]))

(s/fdef config
  :args (s/cat :cfg string?
               :opts ::spec/opts)
  :ret map?)

(defn config
  "Parse the given configuration with any of the specified options."
  ([cfg]
   (config cfg {:profile :default}))
  ([cfg opts]
   (core/config cfg opts)))

(s/fdef env
  :args (s/cat :key keyword?))

(defn env
  "Read a value from the app environment."
  [key]
  (core/env key))

(s/fdef init
  :args (s/cat :cfg map?)
  :ret map?)

(defn init
  "Initialize the app's configuration and dependencies."
  [cfg]
  (core/init cfg))

(defn halt!
  "Stop the app's running dependencies."
  []
  (core/halt!))
