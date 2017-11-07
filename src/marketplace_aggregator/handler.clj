(ns marketplace-aggregator.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as string]
            [hiccup.core :as hiccup]
            [marketplace-aggregator.datasources :as sources]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn search [query location]
  "Searches for the query across marketplaces and returns a list of results"
  (let [craigslist-results (sources/search-craigslist query location)]
    (concat craigslist-results)))

(defn results-header [query location result-count]
  (hiccup/html [:h1 (str
                     result-count
                     (if (= 1 result-count) " result" " results")
                     " found for \""
                     query
                     "\" in "
                     (string/join
                      " "
                      (map string/capitalize
                           (string/split location #" "))))]))

(defn search-form []
  (hiccup/html [:form
                {:method "GET"
                 :action "search"}
                [:input {:type "text"
                         :name "query"
                         :required "true"
                         :placeholder "search query"}
                 [:select {:name "location"}
                  [:option {:value "boston" :selected "true"} "Boston"]
                  [:option {:value "salt lake city"} "Salt Lake City"]]]
                [:input {:type "submit" :value "Search"}]]))

(defn result->html [result]
  (hiccup/html [:li [:span [:a {:href (result :href)} (result :title)]
                     " - "
                     (let [price (result :price)]
                       (if (= price -1)
                         "Price not available"
                         (str "$" (format "%,.2f" price))))]]))

(defn render-results [results]
  "Renders search results to HTML"
  (hiccup/html [:ul (map result->html results)]))

(defroutes app-routes
  (GET "/" [] (search-form))
  (GET "/search" [query location]
       (let [results (search query location)]
         (str (search-form)
              (results-header query location (count results))
              (render-results results))))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
