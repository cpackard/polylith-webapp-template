(ns poly.web.test-utils.core
  (:require
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]))

(defn pretty-spec!
  "Enable pretty printing for spec errors."
  [f]
  (set! s/*explain-out* expound/printer)
  (f))
