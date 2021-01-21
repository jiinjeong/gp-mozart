(defproject push307 "0.1.0"
  :description "PushGP, as implemented by Hamilton College's CS 307 class."
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :main ^:skip-aot push307.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
