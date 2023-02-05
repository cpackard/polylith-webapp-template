(ns poly.web.macros.interface
  (:require
   [poly.web.macros.core :as core]))

(defmacro some-fn->
  "Exactly like `some->` but you pass in the predicate function `pred`.

  These two are equivalent:
  `(some-> {:a {:b 2}} :a :c inc)`
  `(some-fn-> nil? {:a {:b 2}} :a :c inc)`
  "
  [pred expr & forms]
  `(core/some-fn-> ~pred ~expr ~@forms))
