(ns marketplace-aggregator.amazon
  (:require [environ.core :refer [env]]))

(def amazon-access-key (env :amazon-access-key))
(def amazon-secret-key (env :amazon-secret-key))
(def amazon-affiliate-id (env :amazon-affiliate-id))
