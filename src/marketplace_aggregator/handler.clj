(ns marketplace-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as string]
            [marketplace-aggregator.datasources :as sources]
            [marketplace-aggregator.views :as views]
            [marketplace-aggregator.locations :as locations]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn search [query location]
  "Searches for the query across marketplaces and returns a list of results"
  (let [craigslist-results (sources/search-craigslist query (:city location))
        ebay-results (sources/search-ebay query location)]
    (concat craigslist-results
            ebay-results)))

(defroutes app-routes
  (GET "/" [] (views/index))
  (GET "/search" [query location]
       (let [location-map (locations/locations location)
             results (search query location-map)]
         (views/search-results query location results)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
