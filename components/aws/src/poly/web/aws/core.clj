(ns poly.web.aws.core
  (:require
   [clojure.java.io :as io]
   [cognitect.aws.client.api :as aws]
   [cognitect.aws.credentials :as aws-creds]))

(defn s3-client
  [& {:keys [region endpoint-override profile]
      :or {region :us-east-1
           endpoint-override nil
           profile nil}}]
  (let [opts (cond-> {:api :s3
                      :region region}
               endpoint-override (assoc :endpoint-override
                                        endpoint-override)
               profile (assoc :credentials-provider
                              (aws-creds/profile-credentials-provider profile)))]
    (aws/client opts)))

(defn- invoke-aws
  [client op-map]
  (let [client (or client (s3-client))]
    (aws/invoke client op-map)))

(defn s3-upload
  [bucket key filepath
   & {:keys [client]
      :or   {client nil}}]
  (invoke-aws client
              {:op      :PutObject
               :request {:Bucket bucket
                         :Key    key
                         :Body   (io/input-stream filepath)}}))

(defn s3-get
  [bucket key
   & {:keys [client]
      :or   {client nil}}]
  (invoke-aws client
              {:op      :GetObject
               :request {:Bucket bucket
                         :Key    key}}))

(defn s3-list-objects
  [bucket prefix
   & {:keys [client]
      :or   {client nil}}]
  (invoke-aws client
              {:op      :ListObjectsV2
               :request {:Bucket bucket
                         :Prefix prefix}}))
