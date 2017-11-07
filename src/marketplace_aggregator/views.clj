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
       [:div.form-group.col-5
        [:label {:for "query"} "Query"]
        [:input#query.form-control
         {:type "text"
          :name "query"
          :required "true"
          :placeholder "What are you looking for?"}]]
       [:div.form-group.col-5
        [:label {:for "location"} "Location"]
        [:select#location.form-control.custom-select
         {:name "location"
          :required "true"}
         [:option {:value "boston"} "Boston"]
         [:option {:value "salt lake city"} "Salt Lake City"]]]
       [:div.form-group.col-2
        [:label {:for "sort"} "Sort By"]
        [:select#sort.form-control.custom-select
         {:name "sort"}
         [:option {:value "price" :selected "true"} "Price"]
         [:option {:value "source"} "Marketplace"]
         [:option {:value "title"} "Alphabetical"]]]
       [:div.col-12
        [:input.btn.btn-primary
         {:type "submit" :value "Search"}]]]]]]))

(defn result->html [result]
  (hiccup/html [:li.list-group-item
                [:span.badge.badge-pill.badge-light (result :source)]
                [:span " "]
                [:span [:a {:href (result :href)} (result :title)]
                 [:span.float-right
                  (let [price (result :price)]
                    (if (= price -1)
                      "Price not available"
                      (str "$" (format "%,.2f" price))))]]]))

(defn render-results [results sort-key]
  "Renders search results to HTML"
  (hiccup/html [:ul.list-group.list-group-flush
                (map result->html
                     (sort-by sort-key results))]))

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

(defn results-list [query location results sort-key]
  (hiccup/html
   [:div.card
    (results-header query location (count results))
    (render-results results sort-key)]))

(defn page-header []
  (hiccup/html [:h1.display-2
                "Aggregate Marketplace Search"]))

(defn index [] (page-template "Search"
                              (page-header)
                              (search-form)))

(defn search-results [query location results sort-key]
  (page-template
   (str "Search results: " query)
   (page-header)
   (search-form)
   (hiccup/html [:br])
   (results-list query location results sort-key)))
