(ns functional-pharmacy.db
  (:require [datomic.api :as d]))

(defn init-db [name schema seed-data]
  (let [uri (str "datomic:mem://" name)
        conn (do (d/delete-database uri)
                 (d/create-database uri)
                 (d/connect uri))]
    @(d/transact conn schema)
    @(d/transact conn seed-data)
    (d/db conn)))

(defn read-file [s] (read-string (slurp s)))
(defn read-data [s] (read-file (format "resources/db/%s-data.edn" s)))

(defn easy! []
  (do
    (def schema (read-file  "resources/db/schema.edn"))
    (def db-name "fun-pharm-db")
    (def all-the-seeds (into []  (flatten (into [] (concat (map read-data ["drug" "people" "hospital" "prescription"]))))))
    (def db-val (init-db db-name schema all-the-seeds))))
