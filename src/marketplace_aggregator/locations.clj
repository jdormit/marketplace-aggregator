(ns marketplace-aggregator.locations
  (:require [marketplace-aggregator.models :refer [make-location]]
            [clojure.string :as string]))

(defn display-location [location]
  (str
   (string/join " "
                (map string/capitalize
                     (string/split (:city location) #" ")))
   ", " (string/upper-case (:state location))))

(def locations
  {:boston (make-location "boston" "ma" "boston")
   :salt-lake-city (make-location "salt lake city" "ut" "salt lake city")
   :providence (make-location "providence" "ri" "rhode island")
   :chicago (make-location "chicago" "il" "chicago")
   :dover (make-location "dover" "de" "delaware")
   :topeka (make-location "topeka" "ks" "topeka")
   :baltimore (make-location "baltimore" "md" "baltimore")
   :san-diego (make-location "san diego" "ca" "san diego")
   :durham (make-location "durham" "nc" "durham")
   :boise (make-location "boise" "id" "boise")
   :new-york-city (make-location "new york city" "ny" "new york city")})
