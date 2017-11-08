(ns marketplace-aggregator.datasources
  (:require [marketplace-aggregator.craigslist :as craigslist]
            [marketplace-aggregator.ebay :as ebay]))

(defn search-craigslist [query location]
  "Searches for the query on Craigslist"
  (craigslist/search query location))

(defn search-ebay [query location]
  (ebay/search query location))
