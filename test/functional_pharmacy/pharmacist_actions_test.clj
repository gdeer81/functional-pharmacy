(ns functional_pharmacy.pharmacist_actions_test
  (:require [functional_pharmacy.pharmacist_actions :as pa]
            [datomic.api :as d]))

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


(def db-url "datomic:mem://fun-pharm-db")
(d/create-database db-url)
(def conn (d/connect db-url))
(def schema (read-string (slurp "resources/db/schema.edn")))
(def people (read-string (slurp "resources/db/people-data.edn")))
(def drugs (read-string (slurp "resources/db/drug-data.edn")))
(def hospitals (read-string (slurp "resources/db/hospital-data.edn")))
(def prescriptions (read-string (slurp "resources/db/prescription-data.edn")))

(def db (:db-after @(d/transact conn schema)))

(def db-with-people (:db-after (d/with db people)))

(easy!)

;;we can speculate adding a patient
(d/q '[:find ?e ?birthdate
       :in $
       :where [?e :person/name "John Doe"]
       [?e :person/born ?birthdate]] (:db-after (d/with db [(pa/add-patient db "John Doe" #inst "1955-01-02")])))

;;we see that the actual db-val was unchanged
(d/q '[:find ?e ?name ?birthdate
       :in $
       :where [?e :person/name ?name]
              [?e :person/born ?birthdate]] db-val)

;;grab the first entity id out of the database
(def first-eid (ffirst (d/q '[:find ?e
                              :in $
                              :where [?e :person/name "Cam Barker"]] db-val)))

;;grab the first medication id out of the database
(def med-id (ffirst (d/q '[:find ?e :in $ :where [?e :drug/name _]] db-val)))

;;we can add a prescription
(d/q '[:find ?e :in $ ?eid :where [?e :prescription/patient ?eid]] (:db-after (d/with db-val (pa/add-prescription db-val first-eid first-eid med-id 90 #inst "2014-02-04" 3))) first-eid)

(d/q '[:find ?name :in $ ?eid :where [?eid :person/name ?name]] db-val first-eid)

;;we can view all of the prescriptions
(pa/view-all-prescriptions db-val)

;;that's not very interesting since entities are lazy so we have to
;;tell it we use touch to get everything about the entity
(for [e (pa/view-all-prescriptions db-val)]
   (d/touch (d/entity db-val (first e))))

;;lets see what prescriptions Cam Barker has
(def cams-scripts (pa/view-prescription-for-patient db-val "Cam Barker"))

;;how many does he have?
(count cams-scripts)

;;now lets look at everything about it
(d/touch (d/entity db-val (ffirst cams-scripts)))


;;a quick tour of auditing the database

;;the whole history of the database
(def db-history (d/history db-val))

;;when was Cam added to the database
(d/q '[:find ?inst
        :in $
        :where [_ :person/name "Cam Barker" ?tx]
       [?tx :db/txInstant ?inst]] db-history)

;;all the attributes about a given transaction
(d/q '[:find ?inst ?attr
       :in $
       :where [_ :person/name "Cam Barker" ?tx]
       [?tx :db/txInstant ?ins]
       [?inst ?a]
       [?a :db/ident ?attr]] db-history)

(d/q '[:find ?e ?name ?prescriber :where [?p :person/name ?name]
       [?e :prescription/patient ?p]
       [?e :prescription/prescriber ?prid]
       [?prid :person/name ?prescriber]] db-history)
