(ns marketplace-aggregator.locations
  (:require [marketplace-aggregator.models :refer [make-location]]))

(def locations
  {"boston" (make-location "boston" "ma")
   "salt lake city" (make-location "salt lake city" "ut")})
