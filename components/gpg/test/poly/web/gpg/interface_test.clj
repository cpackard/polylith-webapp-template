(ns poly.web.gpg.interface-test
  (:require
   [clj-pgp.core :as pgp]
   [clojure.java.io :as io]
   [clojure.test :as test :refer [deftest is testing use-fixtures]]
   [poly.web.gpg.interface :as gpg]))

;; Run the following commands in your terminal to recreate these:
;; gpg gpg --full-generate-key (choose "RSA and RSA" for the kind of key and use Test123! as the passphrase)
;; gpg --export -a christian@example.com > christian@example.com.pub.gpg
;; gpg --export-secret-key -a christian@example.com > christian@example.com.secret.gpg

(def ^:private content-file
  "File for testing encrypt/decrypt functions."
  (str "dummy-content-file-" (.toString (java.util.UUID/randomUUID)) ".gpg"))

(defn cleanup-test-file
  [f]
  (f)
  (io/delete-file content-file true))

(use-fixtures :each
  cleanup-test-file)

(deftest test--encrypt--decrypt
  (let [pubkey (gpg/load-pubkey (io/as-file (io/resource "gpg/christian@example.com.pub.gpg")))
        passphrase "Test123!"
        privkey (gpg/load-privkey pubkey passphrase (io/as-file (io/resource "gpg/christian@example.com.secret.gpg")))
        content "example-message"]

    (testing "public key has correct format"
      (is (= :rsa-general (-> pubkey pgp/key-info :algorithm))))

    (testing "can write an encrypted message to a file"
      (-> (gpg/encrypt content pubkey)
          (gpg/save-message! content-file))
      (is (true? (-> content-file io/file .exists))))

    (testing "can decrypt message from a file"
      (let [encrypted-msg (slurp content-file)
            decrypted-msg (gpg/decrypt encrypted-msg privkey)]
        (is (= content decrypted-msg))))))
