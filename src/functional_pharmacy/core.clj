(ns functional-pharmacy.core
  (:require [datomic.api :as d]
            [functional-pharmacy.db :as f])
  (:gen-class))

;;set up the database with all the schema and seed data
(f/easy!)

;;get a connection to the db
(def conn (d/connect "datomic:mem://fun-pharm-db"))

(foo "Al Deral" #inst "1954-01-22")
(:user/add-person ["Al Deral" "1901"] )
(d/with f/db-val [[:user/add-person "Al Deral" #inst "1954-01-22"]])x
(d/transact conn [[:add-person db-val (d/tempid :db.part/user) "Al Deral"]])




(defn age [birthday today]
  (quot (- (.getTime today)
           (.getTime birthday))
        (* 1000 60 60 24 365)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
