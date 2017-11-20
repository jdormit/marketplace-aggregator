(defproject marketplace-aggregator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/core.cache "0.6.5"]
                 [compojure "1.5.1"]
                 [environ "1.1.0"]
                 [enlive "1.1.6"]
                 [hiccup "1.0.5"]
                 [clj-http "3.7.0"]
                 [javax.mail/mail "1.4.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-environ "1.1.0"]]
  :ring {:handler marketplace-aggregator.server/app}
  :main marketplace-aggregator.server
  :uberjar-name "marketplace-aggregator.jar"
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
