(ns marketplace-aggregator.models)

(defn make-result [title href price]
  {:title title
   :href href
   :price price})
