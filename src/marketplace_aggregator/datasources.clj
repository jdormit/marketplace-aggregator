(ns marketplace-aggregator.datasources
  (:require [marketplace-aggregator.craigslist :as craigslist]))

(defn search-craigslist [query location]
  "Searches for the query on Craigslist"
  (craigslist/search query location))
