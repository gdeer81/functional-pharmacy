(ns functional-pharmacy.pharmacist-actions
  (:require [datomic.api :as d]
            [functional-pharmacy.db :as f]))

(def create-patient
  "Returns a map that can be added to a transaction to create a new user."
  ;; this function creates a user when that person doesn't already exist?
  ;; is that what the when-not does?
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
  ([db] (d/q '[:find ?person ?birthday
               :in $
               :where [?p :person/name ?person]
                      [?p :person/born ?birthday]] db))

  ([db name] (d/q '[:find ?name ?birthday :in $ ?name :where [?p :person/name ?name]
                                                             [?p :person/born ?birthday]] db name)))

(def create-prescription "Returns a map that can be added to a transaction to create a new prescription for a patient"
  #db/fn {:lang :clojure
          :params [db id patient prescriber medication quantity expiration refills]
          ;; simple logic to prevent accidental double-entries
          :code (when-not (seq (d/q '[:find ?patient ?medication ?expiration
                                      :in $ ?patient ?medication ?expiration
                                      :where [?e :person/name ?name]
                                             [?e :person/born ?birthday]]
                                    db patient medication expiration))
                  {:db/id id
                   :prescription/patient patient
                   :prescription/prescriber prescriber
                   :prescription/medication medication
                   :prescription/quantity quantity
                   :prescription/expiration expiration
                   :prescription/refills refills })})

(defn add-prescription
  "Create a prescription in the database"
  [db patient prescriber medication quantity expiration refills]
  (create-prescription db (d/tempid :db.part/user)
                       patient prescriber medication quantity expiration refills))

(defn view-all-prescriptions
  "View all Prescriptions"
  [db]
  (d/q '[:find ?e :in $ :where [?e :prescription/patient _]] db))

(defn view-prescription-for-patient
  [db name]
  (d/q '[:find ?e :in $ ?name :where
         [?p :person/name ?name]
         [?e :prescription/patient ?p]] db name))
