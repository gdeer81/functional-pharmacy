(ns functional_pharmacy.pharmacist_actions
  (:require [datomic.api :as d]
            [functional-pharmacy.db :as f]))

(def create-patient "reutrns a map that can be added to a transation to create a new user"
  #db/fn {:lang :clojure
          :params [db id name birthday]
          :code (when-not (seq (d/q '[:find ?name ?birthday
                                      :in $ ?name ?birthday
                                      :where [?e :person/name ?name]
                                             [?e :person/born ?birthday]]
                                    db name birthday))
                  {:db/id id
                   :person/name name
                   :person/born birthday})})

(defn add-patient
  "Create a patient in the database return nil if already exists"
  [db name birthday]
  (create-patient db (d/tempid :db.part/user) name birthday))

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
