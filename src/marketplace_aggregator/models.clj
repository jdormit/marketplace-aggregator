(ns marketplace-aggregator.models)

(defn make-result [title href price source]
  {:title title
   :href href
   :price price
   :source source})

(defn make-location [city state craigslist-key]
  {:city city
   :state state
   :craigslist-key craigslist-key})
