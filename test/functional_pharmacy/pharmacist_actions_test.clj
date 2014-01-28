(ns functional_pharmacy.pharmacist_actions_test
  (:require [functional-pharmacy.pharmacist-actions :as pa]
            [datomic.api :as d]))

(def db-url "datomic:mem://testy-db")
(d/create-database db-url)
(def conn (d/connect db-url))
(def schema (read-string (slurp "resources/db/schema.edn")))
(def people (read-string (slurp "resources/db/people-data.edn")))

(def db (:db-after @(d/transact conn schema)))


;;testing pa/add-patient
(d/q '[:find ?name ?birthdate
       :in $
       :where [?e :person/name ?name]
              [?e :person/born ?birthdate]] 
  (:db-after 
    (d/with db [(pa/add-patient db "John Doe" #inst "1955-01-02")])))

;;testing pa/view-patient
(d/q '[:find ?name ?birthdate
       :in $
       :where [?e :person/name ?name]
              [?e :person/born ?birthdate]] db)

;;#_(pa/view-patient (:db-after (d/with db people)))
;;#_(pa/view-patient (:db-after (d/with db people)) "Percey Ledge")

;;testing pa/create-prescription
(d/q '[:find ?patient ?medication ?prescriber
       :in $
       :where [?e :prescription/patient ?patient]
              [?e :prescription/medication ?medication]
              [?e :prescription/prescriber ?prescriber]]
  (:db-after
    (d/with db [(pa/add-prescription db
                                     "patientref" ;;???
                                     "prescriberref"
                                     "medicationref"
                                     10
                                     #inst "2014-02-22")])))


(defn view-prescription
  "View a Prescription. No Arg returns all prescription in the database."
  []
  nil)

(defn create-fill
  "Create a fill transaction for a prescription"
  []
  nil)
