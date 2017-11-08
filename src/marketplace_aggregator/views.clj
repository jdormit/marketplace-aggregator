(ns marketplace-aggregator.views
  (:require [clojure.string :as string]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]))

(defn page-template [title & body]
  (page/html5 [:head
               [:title title]
               [:meta {:name "viewport"
                       :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
               (page/include-js "/js/jquery.min.js")
               (page/include-js "/js/jquery.dataTables.min.js")
               (page/include-js "/js/dataTables.bootstrap4.min.js")
               (page/include-js "/js/script.js")
               (page/include-css "/css/bootstrap.min.css")
               (page/include-css "/css/dataTables.bootstrap4.min.css")]
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
       [:div.form-group.col-sm-6
        [:label {:for "query"} "Query"]
        [:input#query.form-control
         {:type "text"
          :name "query"
          :required "true"
          :placeholder "What are you looking for?"}]]
       [:div.form-group.col-sm-6
        [:label {:for "location"} "Location"]
        [:select#location.form-control.custom-select
         {:name "location"
          :required "true"}
         [:option {:value "boston"} "Boston"]
         [:option {:value "salt lake city"} "Salt Lake City"]]]
       [:div.form-group.col-12
        [:label "Marketplaces"]
        [:div.form-group
         [:div.form-check.form-check-inline
          [:label.form-check-label
           [:input.form-check-input
            {:type "checkbox"
             :name "marketplaces"
             :value "craigslist"
             :checked "true"}]
           "Craigslist"]]
         [:div.form-check.form-check-inline
          [:label.form-check-label
           [:input.form-check-input
            {:type "checkbox"
             :name "marketplaces"
             :value "ebay"
             :checked "true"}]
           "EBay"]]]]
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

(defn results-table [results]
  (hiccup/html [:table#results-table.table.table-responsive
                [:thead.thead-light [:tr
                                     [:th "Price"]
                                     [:th "Item"]
                                     [:th "Marketplace"]]]
                [:tbody (map
                         (fn [result]
                           [:tr
                            [:td
                             {:data-sort (:price result)}
                             (if (= -1 (:price result))
                               "Price not available"
                               (str "$" (format "%,.2f" (:price result))))]
                            [:td [:a {:href (:href result)} (:title result)]]
                            [:td (:source result)]])
                         results)]]))

(defn results-list [query location results]
  (hiccup/html
   [:div.card
    (results-header query location (count results))
    [:div.card-body (results-table results)]]))

(defn page-header []
  (hiccup/html [:h1.display-2
                "Comparison Shopper"]))

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
