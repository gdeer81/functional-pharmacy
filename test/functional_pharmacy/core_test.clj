(ns functional-pharmacy.core-test
  (:require [expectations :refer :all]
            [functional-pharmacy.core]
            [datomic.api :as d]))

(def db-url "datomic:mem://testy-db")
(d/create-database db-url)
(def conn (d/connect db-url))

(def schema (read-string (slurp "resources/db/schema.edn")))
(def people (read-string (slurp "resources/db/people-data.edn")))

(def db (:db-after @(d/transact conn schema)))

(ffirst (d/q '[:find ?name
       :in $
       :where [_ :person/name ?name]] (:db-after (d/with db people))))

;;query database as if a transaction happened
(d/q '[:find ?e ?birthdate
       :in $
       :where [?e :person/name "Clairol Danish"]
              [?e :person/born ?birthdate]] (:db-after (d/with db people)))

;;actual database is unchanged
(d/q '[:find ?e ?birthdate
       :in $
       :where [?e :person/name "Clairol Danish"]
              [?e :person/born ?birthdate]] db)

;;literal transaction data right there in your test
(d/q '[:find ?e ?birthdate
       :in $
       :where [?e :person/name "Al Deral"]
              [?e :person/born ?birthdate]] (:db-after (d/with db [{:db/id (d/tempid :db.part/user)
                                                                     :person/name "Al Deral"
                                                                     :person/born #inst "1954-01-29"}])))







