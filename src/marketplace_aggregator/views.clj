(ns marketplace-aggregator.views
  (:require [clojure.string :as string]
            [environ.core :refer [env]]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [ring.util.anti-forgery :as csrf]
            [marketplace-aggregator.constants :as constants]
            [marketplace-aggregator.locations :as locations])
  (:import [java.net URLEncoder]))

(def ebay-campaign-id (env :ebay-campaign-id))
(def google-analytics-id (env :google-analytics-id))

(defn google-analytics [id]
  (hiccup/html
   [:script {:async "async"
             :src (str "https://www.googletagmanager.com/gtag/js?id=" id)}]
   [:script (str "window.dataLayer = window.dataLayer || [];"
                 "function gtag(){dataLayer.push(arguments);}"
                 "gtag('js', new Date());"
                 "gtag('config', '"
                 id
                 "');")]))

(defn page-template [title & body]
  (page/html5 [:head
               [:title title]
               [:meta {:name "viewport"
                       :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
               (google-analytics google-analytics-id)
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

(defn feedback-form
  ([feedback subject]
   (hiccup/html
    [:div.card
     [:div.card-body
      [:h2.card-title "Submit feedback"]
      [:form {:method "POST"
              :action "feedback"}
       [:div.form-group
        [:label {:for "email"} "Email Address"]
        [:input#email.form-control
         {:type "email"
          :name "email"
          :required "true"
          :placeholder "you@example.com"}]]
       [:div.form-group
        [:label {:for "subject"} "Subject"]
        [:input#subject.form-control
         {:type "text"
          :name "subject"
          :value subject
          :placeholder "Subject"
          :required "true"}]]
       [:div.form-group
        [:label {:for "feedback"} "Feedback"]
        [:textarea#feedback.form-control
         {:name "feedback"
          :required "true"
          :autofocus "autofocus"
          :placeholder "Your feedback"}
         feedback]]
       (csrf/anti-forgery-field)
       [:input.btn.btn-primary
        {:type "submit"
         :value "Submit"}]]]]))
  ([] (feedback-form "" "Comparison Shopper Feedback")))

(defn search-form []
  (hiccup/html
   [:div.card
    [:div.card-body
     [:h2.card-title "Find a product"]
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
          :placeholder "Product Name"}]]
       [:div.form-group.col-sm-6
        [:label {:for "location"} "Location"]
        [:span.float-right
         [:a
          {:href (str "feedback?feedback="
                      (URLEncoder/encode
                       "Please add my city! I live in:")
                      "&subject="
                      (URLEncoder/encode "Add City Request"))}
          "Don't see your city?"]]
        [:select#location.form-control.custom-select
         {:name "location"
          :required "true"}
         (map (fn [loc]
                [:option {:value (name (first loc))}
                 (locations/display-location (second loc))])
              (sort-by #(:city (second %)) locations/locations))]]
       [:div.form-group.col-12
        [:label "Marketplaces"]
        [:div.form-group
         (source-checkbox "Craigslist" "craigslist")
         (source-checkbox "EBay" "ebay")
         (source-checkbox "Amazon" "amazon")]]
       [:div.col-12
        [:input.btn.btn-primary
         {:type "submit" :value "Search"}]
        [:a.float-right.mr-3 {:href "/feedback"} "Submit feedback / Report a bug"]]]]]]))

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
                "Comparison Shopper"]
               [:p.lead.text-muted
                "Find the best deals on the web"]))

(defn index [] (page-template "Comparison Shopper"
                              (page-header)
                              (search-form)))

(defn search-results [query location results]
  (page-template
   (str "Comparison Shopper | " query)
   (page-header)
   (search-form)
   (results-header query location)
   (results-list query location results)))

(defn feedback
  ([feedback subject] (page-template
                       "Comparison Shopper | Feedback"
                       (page-header)
                       (feedback-form feedback subject)))
  ([] (feedback "" "Comparison Shopper Feedback")))

(defn thanks []
  (page-template "Comparison Shopper | Thanks!"
                 (hiccup/html [:div.jumbotron
                               [:h1.display-3 "Thanks for your feedback!"]
                               [:p.lead "We appreciate that you took the time to help us out."]
                               [:a {:href "/"} "Return to Comparison Shopper"]])))
