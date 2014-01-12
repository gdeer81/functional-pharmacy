(ns functional-pharmacy.db
  (:require [datomic.api :as d]))

(defonce db-url "datomic:mem://fn-pharm-db")

(defn db [] (d/db (d/connect db-url)))

(defn tx [t] (d/transact (d/connect db-url)))

(def e (comp d/touch #(d/entity (db) %) first))

(defn init-db [name schema seed-data]
  (let [uri (str "datomic:mem://" name)
        conn (do (d/delete-database uri)
                 (d/create-database uri)
                 (d/connect uri))]
    @(d/transact conn schema)
    @(d/transact conn seed-data)
    (d/db conn)))

(defn read-file [s] (read-string (slurp s)))
;;drug people hospital prescription
(defn read-data [s] (read-file (format "resources/db/%s-data.edn" s)))


(format "resources/db/%s-data.edn" "people")
