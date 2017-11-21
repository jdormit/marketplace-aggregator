(ns marketplace-aggregator.zipcode
  (:require [clojure.core.cache :as cache]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [clj-http.client :as http]
            [environ.core :refer [env]]))

(defonce cache-store (atom (cache/lru-cache-factory {})))
(def api-key (env :zipcode-api-key))

(defn get-zips [location]
  (:zip_codes (json/read-str
    (:body (http/get (str "http://www.zipcodeapi.com/rest/"
                          api-key
                          "/city-zips.json/"
                          (:city location) "/"
                          (:state location))))
    :key-fn #(keyword %))))

(defn lookup [location]
  (let [cache-key (str (string/replace (:city location) #" " "-")
                       "-"
                       (string/replace (:state location) #" " "-"))]
    (cache/lookup (swap! cache-store
                         #(if (cache/has? % cache-key)
                            (cache/hit % cache-key)
                            (cache/miss % cache-key (get-zips location))))
                 cache-key)))

