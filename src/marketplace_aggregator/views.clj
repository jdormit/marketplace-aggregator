(ns marketplace-aggregator.views
  (:require [clojure.string :as string]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]))

(defn page-template [title & body]
  (page/html5 [:head
               [:title title]
               [:meta {:name "viewport"
                       :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
               (page/include-css "/css/bootstrap.min.css")]
              [:body
               [:div.container (apply str body)]]))

(defn search-form []
  (hiccup/html
   [:div.card
    [:div.card-body
     [:h2.card-title "Find an item"]
     [:form
      {:method "GET"
       :action "search"}
      [:div.form-row
       [:div.form-group.col-6
        [:label {:for "query"} "Query"]
        [:input#query.form-control
         {:type "text"
          :name "query"
          :required "true"
          :placeholder "search query"}]]
       [:div.form-group.col-6
        [:label {:for "location"} "Location"]
        [:select#location.form-control.custom-select
         {:name "location"
          :require "true"}
         [:option {:value "boston"} "Boston"]
         [:option {:value "salt lake city"} "Salt Lake City"]]]
       [:div.col-12
        [:input.btn.btn-primary
         {:type "submit" :value "Search"}]]]]]]))

(defn result->html [result]
  (hiccup/html [:li.list-group-item
                [:span.badge.badge-pill.badge-light (result :source)]
                [:span [:a {:href (result :href)} (result :title)]
                 " - "
                 (let [price (result :price)]
                   (if (= price -1)
                     "Price not available"
                     (str "$" (format "%,.2f" price))))]]))

(defn render-results [results]
  "Renders search results to HTML"
  (hiccup/html [:ul.list-group.list-group-flush
                (map result->html results)]))

(defn results-header [query location result-count]
  (hiccup/html [:div.card-header
                (str
                 result-count
                 (if (= 1 result-count) " result" " results")
                 " found for \""
                 query
                 "\" in "
                 (string/join
                  " "
                  (map string/capitalize
                       (string/split location #" "))))]))

(defn results-list [query location results]
  (hiccup/html
   [:div.card
    (results-header query location (count results))
    (render-results results)]))

(defn page-header []
  (hiccup/html [:h1.display-2
                "Aggregate Marketplace Search"]))

(defn index [] (page-template "Search"
                              (page-header)
                              (search-form)))

(defn search-results [query location results]
  (page-template
   (str "Search results: " query)
   (page-header)
   (search-form)
   (hiccup/html [:br])
   (results-list query location results)))
