(ns functional-pharmacy.core-test
  (:require [expectations :refer :all]
            [functional-pharmacy.core]
            [datomic.api :as d]))

(def db-url "datomic:mem://testy-db")
(d/create-database db-url)
(def conn (d/connect db-url))

(def schema (read-string (slurp "resources/db/schema.edn")))
(def people (read-string (slurp "resources/db/people-data.edn")))
people
(def db (:db-after @(d/transact conn  schema)))

;;DO NOT DO THIS (def people-db @(d conn people))

(d/q '[:find ?name
       :in $
       :where [_ :person/name ?name]] (:db-after (d/with db people)))
