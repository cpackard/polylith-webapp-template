(ns poly.web.logging.core
  (:require
   [io.pedestal.log :as log]))

;; inspired by https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app/blob/master/components/log/src/clojure/realworld/log/core.clj#L10
(defmacro info
  ([msg]
   `(log/info :msg ~msg))
  ([msg & args]
   `(log/info :msg ~msg ~@args)))

(defmacro warn
  ([msg]
   `(log/warn :msg ~msg))
  ([msg & args]
   `(log/warn :msg ~msg ~@args)))

(defmacro error
  ([msg]
   `(log/error :msg ~msg))
  ([msg & args]
   `(log/error :msg ~msg ~@args)))
