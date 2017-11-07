(ns marketplace-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            [marketplace-aggregator.datasources :as sources]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn search [query]
  "Searches for the query across marketplaces and returns a list of results"
  (concat (sources/search-craigslist query)))

(defn result->html [result]
  (hiccup/html [:li [:span [:a {:href (result :href)} (result :title)]
                     " - "
                     (str "$" (result :price))]]))

(defn render-results [results]
  "Renders search results to HTML"
  (hiccup/html [:ul (map result->html results)]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/search" [query]
       (render-results (search query)))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
