(ns poly.web.aws.interface
  (:require
   [poly.web.aws.core :as core]
   [poly.web.aws.interface :as aws]))

(defn s3-client
  "Create an S3 client."
  [& {:keys [region endpoint-override profile]
      :or {region :us-east-1
           endpoint-override nil
           profile nil}}]
  (core/s3-client
    :region region
    :endpoint-override endpoint-override
    :profile profile))

(defn s3-upload
  "Upload a file to S3."
  [bucket key filepath
   & {:keys [client]
      :or   {client nil}}]
  (core/s3-upload key filepath :bucket bucket :client client))

(defn s3-get
  "Get a file from S3."
  [bucket key
   & {:keys [client]
      :or   {client nil}}]
  (core/s3-get key :bucket bucket :client client))

(defn s3-list-objects
  "List objects in an S3 bucket."
  [bucket prefix
   & {:keys [client]
      :or   {client nil}}]
  (core/s3-list-objects prefix :bucket bucket :client client))

(comment
  (require '[poly.web.aws.interface :as aws])
  (let [s3 (aws/s3-client :profile "prod-temp")
        bucket "poly-web-temp"
        doc-prefix "docs/"]
    (aws/s3-list-objects doc-prefix :bucket bucket :client s3)))
