(ns poly.web.config.interface
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.config.core :as core]
   [poly.web.config.interface.spec :as spec]))

(def default-secret-value "default-secret-please-change!")

(s/def ::opts (s/keys :req-un [::spec/profile]))

(s/fdef config
  :args (s/cat :cfg string?
               :opts (s/? ::opts))
  :ret map?)

(defn parse
  "Parse the given configuration with any of the specified options."
  ([cfg]
   (parse cfg {:profile :default}))
  ([cfg opts]
   (core/parse cfg opts)))

(s/fdef parse-cfgs
  :args (s/cat :cfg (s/coll-of string?)
               :opts (s/? ::opts))
  :ret map?)

(defn parse-cfgs
  "Parse all configs requested in `cfgs` into a single merged map."
  [cfgs opts]
  (core/parse-cfgs cfgs opts))

(s/fdef init
  :args (s/cat :cfg map?)
  :ret map?)

(defn init
  "Initialize the app's configuration and dependencies."
  [cfg]
  (core/init cfg))

(defn halt!
  "Stop the app's running dependencies."
  [sys]
  (core/halt! sys))
