(ns marketplace-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as string]
            [marketplace-aggregator.datasources :as sources]
            [marketplace-aggregator.views :as views]
            [marketplace-aggregator.locations :as locations]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn search [query marketplaces location]
  "Searches for the query across marketplaces and returns a list of results"
  (concat (if (some #{"craigslist"} marketplaces)
            (sources/search-craigslist query (:city location))
            nil)
          (if (some #{"ebay"} marketplaces)
            (sources/search-ebay query location)
            nil)))

(defroutes app-routes
  (GET "/" [] (views/index))
  (GET "/search" [query location marketplaces]
       (let [location-map (locations/locations location)
             marketplaces (if (vector? marketplaces) marketplaces [marketplaces])
             results (search query marketplaces location-map)]
         (views/search-results query location results)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
