(ns marketplace-aggregator.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as string]
            [environ.core :refer [env]]
            [marketplace-aggregator.datasources :as sources]
            [marketplace-aggregator.views :as views]
            [marketplace-aggregator.locations :as locations]
            [marketplace-aggregator.constants :as constants]
            [marketplace-aggregator.gmail :as gmail]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def gmail-password (env :gmail-password))
(def gmail-username (env :gmail-username))

(defn search [query marketplaces location]
  "Searches for the query across marketplaces and returns a list of results"
  (sort-by :price
           (take constants/num-results
                 (concat (if (some #{"craigslist"} marketplaces)
                           (sources/search-craigslist query location)
                           nil)
                         (if (some #{"ebay"} marketplaces)
                           (sources/search-ebay query location)
                           nil)
                         (if (some #{"amazon"} marketplaces)
                           (sources/search-amazon query location)
                           nil)))))

(defroutes app-routes
  (GET "/" [] (views/index))
  (GET "/search" [query location marketplaces]
       (let [location-map (locations/locations (keyword location))
             marketplaces (if (vector? marketplaces) marketplaces [marketplaces])
             results (search query marketplaces location-map)]
         (views/search-results query location results)))
  (GET "/feedback" [feedback subject]
       (views/feedback feedback subject))
  (POST "/feedback" [email subject feedback]
        (gmail/send-mail {:from email
                          :to ["feedback@comparisonshopper.io"]
                          :subject subject
                          :text feedback
                          :user gmail-username
                          :password gmail-password})
        (response/redirect "/thanks"))
  (GET "/thanks" [] (views/thanks))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn -main [& args]
  (jetty/run-jetty app
                   {:port 3000}))
