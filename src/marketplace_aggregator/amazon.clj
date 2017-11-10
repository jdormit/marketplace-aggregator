(ns marketplace-aggregator.amazon
  (:require [environ.core :refer [env]])
  (:import [java.util.Date
            java.text.SimpleDateFormat
            java.net.URLEncoder
            javax.crypto.Mac
            javax.crypto.spec.SecretKeySpec
            org.apache.commons.codec.binary.Base64]))

(def amazon-access-key (env :amazon-access-key))
(def amazon-secret-key (env :amazon-secret-key))
(def amazon-affiliate-id (env :amazon-affiliate-id))

(defn now-timestamp []
  (let [formatter (SimpleDateFormat. "YYYY-MM-DDThh:mm:ssZ")]
    (.format formatter (Date.))))

(defn hmac-hash [key data]
  (let [mac (Mac/getInstance "HmacSHA256")
        secret-key (SecretKeySpec. (.getBytes key) "HmacSHA256")]
    (do (.init mac secret-key)
        (Base64/encodeBase64String
         (.doFinal (.getBytes data))))))

(defn gen-signature [keywords timestamp]
  (let [canonical-string (-> ["Service=AWSECommerceServer"
                              (str "AWSAccessKeyId=" amazon-access-key)
                              (str "AssociateTag=" amazon-affiliate-id)
                              "Operation=ItemSearch"
                              (str "Keywords=" (URLEncoder/encode keywords))
                              "SearchIndex=All"
                              (str "Timestamp=" timestamp)]
                             (sort)
                             (join "&"))]
    (hmac-hash amazon-secret-key
               (str "GET\nwebservices.amazon.com\n/onca/xml\n"
                    canonical-string))))

(defn itemlookup-url [keywords]
  (let [timestamp (now-timestamp)]
    (str "http://webservices.amazon.com/onca/xml"
        "?Service=AWSECommerceServer"
        "&AWSAccessKeyId=" amazon-access-key
        "&AssociateTag=" amazon-affiliate-id
        "&Operation=ItemSearch"
        "&Keywords=" (URLEncoder/encode keywords)
        "&SearchIndex=All"
        "&Timestamp=" timestamp
        "&Signature=" (gen-signature keywords timestamp))))
