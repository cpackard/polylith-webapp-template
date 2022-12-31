(ns poly.web.logging.core
  (:require
   [io.pedestal.log :as log]))

;; inspired by https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app/blob/master/components/log/src/clojure/realworld/log/core.clj#L10
(defmacro info
  [msg]
  `(log/info :msg ~msg))

(defmacro warn
  [msg]
  `(log/warn :msg ~msg))

(defmacro error
  [msg]
  `(log/error :msg ~msg))
