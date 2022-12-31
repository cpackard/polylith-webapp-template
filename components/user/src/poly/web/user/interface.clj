(ns poly.web.user.interface
  (:require
   [integrant.core :as ig]))

(defmethod ig/pre-init-spec ::secret
  [_]
  string?)

(defmethod ig/init-key ::secret
  [_ secret]
  secret)
