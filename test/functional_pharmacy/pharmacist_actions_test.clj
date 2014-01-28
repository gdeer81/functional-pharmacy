(ns functional_pharmacy.pharmacist_actions_test
  (:require [functional_pharmacy.pharmacist_actions :as pa]
            [datomic.api :as d]))


(def db-url "datomic:mem://testy-db")
(d/create-database db-url)
(def conn (d/connect db-url))
(def schema (read-string (slurp "resources/db/schema.edn")))
(def people (read-string (slurp "resources/db/people-data.edn")))

(def db (:db-after @(d/transact conn schema)))

(d/q '[:find ?e ?birthdate
       :in $
       :where [?e :person/name "John Doe"]
              [?e :person/born ?birthdate]] (:db-after (d/with db [(pa/add-patient db "John Doe" #inst "1955-01-02")])))

(defn view-patient
  "View a patient. No Arg returns all patients in the database."
  []
  nil)

(defn create-prescription
  "Create a prescription in the database"
  []
  nil)

(defn view-prescription
  "View a Prescription. No Arg returns all prescription in the database."
  []
  nil)

(defn create-fill
  "Create a fill transaction for a prescription"
  []
  nil)
