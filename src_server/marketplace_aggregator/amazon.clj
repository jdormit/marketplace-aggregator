(ns marketplace-aggregator.amazon
  (:require [clojure.string :as string]
            [clojure.data.xml :as xml]
            [clj-http.client :as http]
            [environ.core :refer [env]]
            [marketplace-aggregator.models :as models])
  (:import (java.util Date TimeZone)
           (java.text SimpleDateFormat)
           (java.net URLEncoder)
           (javax.crypto Mac)
           (javax.crypto.spec SecretKeySpec)
           (org.apache.commons.codec.binary Base64)))

(def amazon-access-key (env :amazon-access-key))
(def amazon-secret-key (env :amazon-secret-key))
(def amazon-affiliate-id (env :amazon-affiliate-id))

(defn encode [str]
  (-> str
      (URLEncoder/encode "ISO-8859-1")
      (string/replace #"\+" "%20")
      (string/replace #":" "%3B")))

(defn now-timestamp []
  (let [formatter (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss'Z'")
        timezone (TimeZone/getTimeZone "UTC")]
    (do (.setTimeZone formatter timezone)
        (.format formatter (Date.)))))

(defn hmac-hash [key data]
  (let [mac (Mac/getInstance "HmacSHA256")
        secret-key (SecretKeySpec. (.getBytes key) "HmacSHA256")]
    (do (.init mac secret-key)
        (Base64/encodeBase64String
         (.doFinal mac (.getBytes data))))))

(defn request-params [keywords timestamp]
  ["Service=AWSECommerceServer"
   (str "AWSAccessKeyId=" amazon-access-key)
   (str "AssociateTag=" amazon-affiliate-id)
   "Operation=ItemSearch"
   (str "Keywords=" (encode keywords))
   "SearchIndex=All"
   (str "ResponseGroup=" (encode "OfferFull,Small"))
   (str "Timestamp=" (encode timestamp))])

(defn gen-signature [keywords timestamp]
  (let [canonical-string (->> (request-params keywords timestamp)
                              (sort)
                              (string/join "&"))]
    (hmac-hash amazon-secret-key
               (str "GET\nwebservices.amazon.com\n/onca/xml\n"
                    canonical-string))))

(defn itemlookup-url [keywords]
  (let [timestamp (now-timestamp)]
    (str "http://webservices.amazon.com/onca/xml?"
         (string/join "&" (request-params keywords timestamp))
         "&Signature=" (encode (gen-signature keywords timestamp)))))

(defn fetch-amazon-results [keywords]
  (xml/parse-str (:body (http/get (itemlookup-url keywords)))))

(defn get-items-from-results [results]
  (filter #(= :Item (:tag %))
          (:content (first
                     (filter #(= :Items (:tag %))
                             (:content results))))))

(defn get-title [item]
  (let [attrs (first (filter #(= :ItemAttributes (:tag %)) (:content item)))]
    (first (:content (first (filter #(= :Title (:tag %)) (:content attrs)))))))

(defn get-offers-href [item]
  (let [offers (first (filter #(= :Offers (:tag %)) (:content item)))]
    (first (:content (first (filter #(= :MoreOffersUrl (:tag %)) (:content offers)))))))

;; TODO handle misleading case where min price is tiny but shipping fee is huge
(defn get-min-price [item]
  (let [offer-summary (first
                       (filter #(= :OfferSummary (:tag %))
                               (:content item)))
        prices (filter #(string/includes? (name (:tag %)) "Lowest")
                       (:content offer-summary))]
    (/ (apply min (map (fn [offer]
                     (Integer/parseInt (first
                                        (:content
                                         (first
                                          (filter (fn [price] (= :Amount (:tag price)))
                                                  (:content offer)))))))
                       prices))
       100.0)))

(defn parse-amazon-results [results]
  (map #(models/make-result
         (get-title %)
         (get-offers-href %)
         (get-min-price %)
         "Amazon")
       (get-items-from-results results)))

(defn search [query]
  (parse-amazon-results (fetch-amazon-results query)))
