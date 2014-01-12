(ns functional-pharmacy.core
  (:require [datomic.api :as d]
            [functional-pharmacy.db :as f])
  (:gen-class))

(def schema (f/read-file  "resources/db/schema.edn"))
(f/init-db "fun-pharm-db" schema hospital-peeps)

(def hospital-peeps (into []  (flatten (into [] (concat (map f/read-data ["drug" "people" "hospital"]))))))


(into [] (concat [1 2 3] [4 5]))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
