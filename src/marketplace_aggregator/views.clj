(ns marketplace-aggregator.views
  (:require [clojure.string :as string]
            [environ.core :refer [env]]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [marketplace-aggregator.constants :as constants]))

(def ebay-campaign-id (env :ebay-campaign-id))

(defn page-template [title & body]
  (page/html5 [:head
               [:title title]
               [:meta {:name "viewport"
                       :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
               [:script (str "window._epn = {campaign:" ebay-campaign-id "};")]
               (page/include-js "/js/epn-smart-tools.js")
               (page/include-js "/js/jquery.min.js")
               (page/include-js "/js/jquery.dataTables.min.js")
               (page/include-js "/js/dataTables.bootstrap4.min.js")
               (page/include-js "/js/script.js")
               (page/include-css "/css/bootstrap.min.css")
               (page/include-css "/css/dataTables.bootstrap4.min.css")]
              [:body
               [:div.container (apply str body)]]))

(defn source-checkbox [name value]
  [:div.form-check.form-check-inline
   [:label.form-check-label
    [:input.form-check-input
     {:type "checkbox"
      :name "marketplaces"
      :value value
      :checked "true"}]
    name]])

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
         (source-checkbox "Craigslist" "craigslist")
         (source-checkbox "EBay" "ebay")
         (source-checkbox "Amazon" "amazon")]]
       [:div.col-12
        [:input.btn.btn-primary
         {:type "submit" :value "Search"}]]]]]]))

(defn result->html [result]
  (hiccup/html [:div.card
                [:div.card-body
                 [:span.badge.badge-pill.badge-light (result :source)]
                 [:span " "]
                 [:span [:a {:href (result :href)} (result :title)]]
                 [:span.float-right
                  (let [price (result :price)]
                    (if (= price -1)
                      "Price not available"
                      (str "$" (format "%,.2f" price))))]]]))

(defn results-header [query location]
  (hiccup/html [:p.lead.font-weight-bold.mt-3
                (str "Here are the "
                     constants/num-results
                     " best prices we found for \""
                     query
                     "\" in "
                     (string/join
                      " "
                      (map string/capitalize
                           (string/split location #" ")))
                     ":")]))

(defn results-table [results]
  (let [results (filter #(not= -1 (:price %)) results)]
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
                               (str "$" (format "%,.2f" (:price result)))]
                              [:td [:a {:href (:href result)} (:title result)]]
                              [:td (:source result)]])
                           results)]])))

(defn results-list [query location results]
  (hiccup/html (map result->html results)))

(defn page-header []
  (hiccup/html [:h1.display-3
                "Comparison Shopper"]))

(defn info-card [title body]
  (hiccup/html [:div.card
                [:div.card-body
                 [:h4.card-title title]
                 [:p.card-text body]]]))

(defn how-it-works []
  (hiccup/html [:h1.mt-2.mb-2 "How It Works"]
               [:div.card-deck
                (info-card "Search"
                           "Input the name of the thing you want to buy. The more specific your search term, the better the results will be - for example, \"Casio CDP-100\" instead of \"keyboard\".")
                (info-card "Browse"
                           (str "Our industrious bargain-hunting robots will scour the web for the best deals for your item. You'll get a list of the "
                                constants/num-results " best prices."))
                (info-card "Save"
                           "Let Comparison Shopper take the stress out of shopping online! We find the best deals so that you don't have to, saving you time and money!")]))

(defn index [] (page-template "Search"
                              (page-header)
                              (search-form)
                              (how-it-works)))

(defn search-results [query location results]
  (page-template
   (str "Search results: " query)
   (page-header)
   (search-form)
   (results-header query location)
   (results-list query location results)))
