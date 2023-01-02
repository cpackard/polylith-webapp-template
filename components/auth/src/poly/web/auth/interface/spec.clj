(ns poly.web.auth.interface.spec
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.auth.spec :as auth-spec]))

(s/def ::jwt auth-spec/jwt)

(s/def ::jwt-str? auth-spec/jwt-str?)
