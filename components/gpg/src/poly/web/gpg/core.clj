(ns poly.web.gpg.core
  (:require
   [clj-pgp
    [message :as pgp-msg]
    [core :as pgp]
    [keyring :as keyring]]
   [clojure.string]))

(defn load-pubkey
  "Load the public key saved in the given file."
  [keyfile]
  (let [pub-keyring-coll (keyring/load-public-keyring keyfile)
        pub-keyring (first pub-keyring-coll)]
    (first pub-keyring)))

(defn load-privkey
  "Load the private key saved in the given file and encrypted with passphrase."
  [pubkey passphrase keyfile]
  (let [priv-keyring-coll (keyring/load-secret-keyring keyfile)
        priv-keyring (first priv-keyring-coll)
        secret-key (keyring/get-secret-key priv-keyring (pgp/hex-id pubkey))]
    (pgp/unlock-key secret-key passphrase)))

(defn encrypt
  "Encrypt the given content "
  [content pubkey]
  (pgp-msg/encrypt
   content pubkey
   :format :utf8
   :cipher :aes-256
   :compress :zip
   :armor true))

(defn save-message!
  "Save the given message to the file located at f."
  [message f]
  (spit f message))

(comment
  (def content "postgresql://my_user:Test123!@localhost/my_db")
  (save-message! (encrypt content pubkey) "~/db-file.gpg"))

(defn decrypt
  "Decrypt the given message using the private key."
  [message privkey]
  (pgp-msg/decrypt message privkey))

(comment
  (defn db-conn-str
    [passphrase]
    (decrypt (slurp "db-file.gpg") (load-privkey (load-pubkey) passphrase (load-pubkey "~/.gnupg/pub.gpg"))))
  (println (db-conn-str (.readPassword (System/console) "GPG passphrase: "))))
