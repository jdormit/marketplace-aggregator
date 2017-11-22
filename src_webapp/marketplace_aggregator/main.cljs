(ns marketplace-aggregator.main)

(defn search-form-submit-callback [event]
  (.preventDefault event)
  (let [placepicker (.data (js/$ "#location") "placepicker")
        lat-lng (.getLatLng placepicker)
        lat (.lat lat-lng)
        lng (.lng lat-lng)]
    (.val (js/$ "#lat-lng")
          (str lat "," lng))
    (this-as form (.submit form))))

(defn jquery-stuff []
  (let [placepicker (.data (.placepicker (js/$ "#location"))
                           "placepicker")
        search-form (js/$ "#search-form")]
    (.geoLocation placepicker)
    (.on search-form "submit" search-form-submit-callback)))

(.ready (js/$ "document") jquery-stuff)
