(ns marketplace-aggregator.main)

(defn jquery-stuff []
  (.placepicker (js/$ "#location")))

(.ready (js/$ "document") jquery-stuff)
