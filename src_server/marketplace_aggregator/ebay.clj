(ns marketplace-aggregator.ebay
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clj-http.client :as http]
            [marketplace-aggregator.zipcode :as zipcode])
  (:import [java.net URLEncoder]))

(defn fetch-ebay-results [query location]
  (let [zip (first (zipcode/lookup location))]
    (json/read-str
    (:body (http/get
            (str "http://svcs.ebay.com/services/search/FindingService/v1"
                 "?OPERATION-NAME=findItemsByKeywords"
                 "&SECURITY-APPNAME=JeremyDo-marketpl-PRD-5134e8f72-d719e529"
                 "&RESPONSE-DATA-FORMAT=JSON"
                 "&buyerPostalCode=" zip
                 "&sortOrder=Distance"
                 "&keywords=" (URLEncoder/encode query))))
    :key-fn #(keyword %))))

(defn parse-ebay-results [results]
  (map (fn [item]
         {:title (first (:title item))
          :href (first (:viewItemURL item))
          :price (let [price
                       (:__value__ (first (:currentPrice (first (:sellingStatus item)))))]
                   (if (string/blank? price)
                     -1
                     (Float/parseFloat price)))
          :source "EBay"})
       (:item (first (:searchResult (first (:findItemsByKeywordsResponse results)))))))

(defn search [query location]
  (parse-ebay-results (fetch-ebay-results query location)))
