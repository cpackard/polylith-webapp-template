(ns poly.web.auth.interface-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as st]
   [clojure.test :as test :refer [deftest is testing use-fixtures]]
   [expound.alpha :as expound]
   [poly.web.auth.interface :as auth]
   [poly.web.logging.interface.test-utils :as log-tu]
   [poly.web.spec.interface :as spec]))

(defn prep-expound-for-tests
  [f]
  (set! s/*explain-out* expound/printer)
  (f))

(def ^:private silenced-ns #{"com.zaxxer.*"
                             "migratus.*"
                             "io.pedestal.*"
                             "poly.web.auth.*"})
(def ^:private log-cfg {:min-level [[silenced-ns :error]]})

(use-fixtures :once
  prep-expound-for-tests
  (log-tu/set-log-config log-cfg))

(s/fdef spec-test
  :args (s/cat :sym qualified-symbol?))

(defn spec-test
  [sym]
  (let [opts      {:clojure.spec.test.check/opts {:num-tests 1000}}
        gen-tests (st/check sym opts)]
    (is (empty? (filter :failure gen-tests))
        (expound/explain-results-str gen-tests))))

(deftest generate-token
  (spec-test `auth/generate-token))

(deftest token->claims
  (spec-test `auth/token->claims))

(deftest check--encrypt-password
  (let [pw-a (gen/generate (s/gen spec/password?))
        pw-b (gen/generate (s/gen spec/password?))
        encrypted-pw-a (auth/encrypt-password pw-a)
        encrypted-pw-b (auth/encrypt-password pw-b)]
    (testing "returns false for password mismatch"
      (is (not (auth/check pw-a encrypted-pw-b))))
    (testing "returns true for password match"
      (is (some? (auth/check pw-a encrypted-pw-a))))))
