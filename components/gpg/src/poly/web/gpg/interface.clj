(ns poly.web.gpg.interface
  (:require
   [poly.web.gpg.core :as core]))

(defn load-pubkey
  "Load the public key saved in the given file."
  ([]
   (core/load-pubkey))
  ([keyfile]
   (core/load-pubkey keyfile)))

(defn load-privkey
  "Load the private key saved in the given file and encrypted with passphrase."
  ([pubkey passphrase]
   (core/load-privkey pubkey passphrase))
  ([pubkey passphrase keyfile]
   (core/load-privkey pubkey passphrase keyfile)))

(defn encrypt
  "Encrypt the given content."
  [content pubkey]
  (core/encrypt content pubkey))

(defn save-message!
  "Save the given message to the file located at f."
  [message f]
  (core/save-message! message f))

(defn decrypt
  "Decrypt the given message using the private key."
  [message privkey]
  (core/decrypt message privkey))
