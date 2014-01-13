(ns functional-pharmacy.core
  (:require [datomic.api :as d]
            [functional-pharmacy.db :as f])
  (:gen-class))

(def schema (f/read-file  "resources/db/schema.edn"))
(def db-name "fun-pharm-db")
(def all-the-seeds (into []  (flatten (into [] (concat (map f/read-data ["drug" "people" "hospital" "prescription"]))))))
(def db-val (f/init-db db-name schema all-the-seeds))
(defn age [birthday today]
  (quot (- (.getTime today)
           (.getTime birthday))
        (* 1000 60 60 24 365)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
