(ns marketplace-aggregator.models)

(defn make-result [title href price source]
  {:title title
   :href href
   :price price
   :source source})
