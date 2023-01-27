(ns poly.web.test-utils.interface
  (:require
   [poly.web.test-utils.core :as core]))

(defn pretty-spec!
  "Enable pretty printing for spec errors."
  [f]
  (core/pretty-spec! f))
