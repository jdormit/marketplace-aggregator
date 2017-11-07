(ns marketplace-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [marketplace-aggregator.datasources :as sources]
            [marketplace-aggregator.views :as views]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn search [query location]
  "Searches for the query across marketplaces and returns a list of results"
  (let [craigslist-results (sources/search-craigslist query location)]
    (concat craigslist-results)))

(defroutes app-routes
  (GET "/" [] (views/index))
  (GET "/search" [query location]
       (let [results (search query location)]
         (views/search-results query location results)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
