(ns marketplace-aggregator.datasources
  (:require [marketplace-aggregator.craigslist :as craigslist]
            [marketplace-aggregator.ebay :as ebay]
            [marketplace-aggregator.amazon :as amazon]))

(defn search-craigslist [query location]
  "Searches for the query on Craigslist"
  (craigslist/search query location))

(defn search-ebay [query location]
  (ebay/search query location))

(defn search-amazon [query location]
  (amazon/search query))
