(defproject functional-pharmacy "0.1.0-SNAPSHOT"
  :description "A pharmacy system that uses Datomic"
  :url "https://github.com/gdeer81/functional-pharmacy"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.datomic/datomic-free "0.9.4384"]]
  :main functional-pharmacy.core
  :profiles {:uberjar {:aot :all}})
