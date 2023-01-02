(ns poly.web.spec.interface
  (:require
   [poly.web.spec.core :as core]))

(def non-empty-string? core/non-empty-string?)

(def email? core/email?)

(def password? core/password?)
