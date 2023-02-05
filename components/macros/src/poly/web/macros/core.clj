(ns poly.web.macros.core)

(defmacro some-fn->
  "Exactly like `some->` but you pass in the predicate function `pred`.

  These two are equivalent:
  `(some-> {:a {:b 2}} :a :c inc)`
  `(some-fn-> nil? {:a {:b 2}} :a :c inc)`
  "
  [pred expr & forms]
  (let [g (gensym)
        steps (map (fn [step] `(if (~pred ~g) ~g (-> ~g ~step)))
                   forms)]
    `(let [~g ~expr
           ~@(interleave (repeat g) (butlast steps))]
       ~(if (empty? steps)
          g
          (last steps)))))
