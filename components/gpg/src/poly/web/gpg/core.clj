(ns poly.web.gpg.core
  (:require
   [clj-pgp
    [message :as pgp-msg]
    [core :as pgp]
    [keyring :as keyring]]
   [clojure.java.io :as io]
   [clojure.string]))

(defn load-pubkey
  "Load the public key saved in the given file."
  [keyfile]
  (let [pub-keyring-coll (keyring/load-public-keyring keyfile)
        pub-keyring      (first pub-keyring-coll)]
    (first pub-keyring)))

(defn load-privkey
  "Load the private key saved in the given file and encrypted with passphrase."
  [pubkey passphrase keyfile]
  (let [priv-keyring-coll (keyring/load-secret-keyring keyfile)
        priv-keyring      (first priv-keyring-coll)
        secret-key        (keyring/get-secret-key priv-keyring (pgp/hex-id pubkey))]
    (pgp/unlock-key secret-key passphrase)))

(defn encrypt
  "Encrypt the given content "
  [content pubkey]
  (pgp-msg/encrypt content
                   pubkey
                   :format :utf8
                   :cipher :aes-256
                   :compress :zip
                   :armor true))

(defn save-message!
  "Save the given message to the file located at f."
  [message f]
  (with-open [file (io/writer f)]
    (.write file message)))

(defn decrypt
  "Decrypt the given message using the private key."
  [message privkey]
  (pgp-msg/decrypt message privkey))

(comment
  (do
    (println "Enter GPG Passphrase:")
    (let [passphrase (read-line) ; Test123!
          pubkey   (load-pubkey (io/as-file (io/resource "gpg/christian@example.com.pub.gpg")))
          privkey  (load-privkey pubkey passphrase (io/as-file (io/resource "gpg/christian@example.com.secret.gpg")))
          content  "postgresql://my_user:Test123!@localhost/my_db"
          filename "db-file.gpg"]
      (save-message! (encrypt content pubkey) filename)
      (println "Encrypted file contents:")
      (println (decrypt (slurp filename) privkey))
      (io/delete-file filename true))))
