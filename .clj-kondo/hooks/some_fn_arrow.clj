(ns hooks.some-fn-arrow
  (:require
   [clj-kondo.hooks-api :as api]))

(defn some-fn-> [{:keys [:node]}]
  (let [[pred expr & forms] (rest (:children node))]
    (when-not (and pred expr)
      (throw (ex-info "No pred and expr given" {})))
    (letfn [(expand-arrow-fn [{[pred-fn & args] :children}]
              (api/list-node [(api/token-node apply) pred-fn expr args]))]
      (if (empty? forms)
        expr
        {:node (api/list-node (map expand-arrow-fn forms))}))))
