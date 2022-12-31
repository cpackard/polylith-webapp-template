(ns poly.web.config.spec
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::profile keyword?)

(s/def ::opts (s/keys :req-un [::profile]))
