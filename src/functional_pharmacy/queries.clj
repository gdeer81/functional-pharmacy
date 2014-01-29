(ns functional-pharmacy.queries
  (:require [datomic.api :as d]
            [functional-pharmacy.db :as f]))

(defn init-db [name schema seed-data]
  (let [uri (str "datomic:postgres://" name)
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


(defn age [birthday today]
  (quot (- (.getTime today)
           (.getTime birthday))
        (* 1000 60 60 24 365)))

(easy!)
;;this returns every person's name date of birth and which hospital they work at
(d/q '[:find ?name ?DOB ?hospital-name :in $ :where [?e :person/name ?name]
       [?e :person/born ?DOB]
       [?heid :hospital/staff ?e]
       [?heid :hospital/title ?hospital-name]] db-val)

;;find all the hospitals built since 1990
(d/q '[:find ?title
 :in $ ?year
 :where
 [?m :hospital/title ?title]
 [?m :hospital/year ?y]
 [(>= ?y ?year)]] db-val 1990)

;;find everyone in the database who is 32
;;note that even those this query is in the same namespace as the function it is using you must fully qualify the function name
(d/q '[:find ?name
 :in $ ?age ?today
 :where
 [?p :person/name ?name]
 [?p :person/born ?born]
       [(functional-pharmacy.queries/age ?born ?today) ?age]] db-val 32 (identity #inst "2014-01-13"))

;;find people younger than Gary Beard and show their ages
(d/q '[:find ?name ?age
 :in $ ?today
 :where
 [?p :person/name "Gary Beard"]
 [?p :person/born ?sborn]
 [?p2 :person/name ?name]
 [?p2 :person/born ?born]
 [(< ?sborn ?born)]
       [(functional-pharmacy.queries/age ?born ?today) ?age]] db-val (identity #inst "2014-01-13"))

;;The birthday paradox states that in a room of 23 people there is a 50% chance that someone has the same birthday.
;;this query finds who has the same birthday.
(d/q '[:find ?name-1 ?name-2
 :where
 [?p1 :person/name ?name-1]
 [?p2 :person/name ?name-2]
 [?p1 :person/born ?born-1]
 [?p2 :person/born ?born-2]
 [(.getMonth ?born-1) ?m]
 [(.getMonth ?born-2) ?m]
 [(.getDate ?born-1) ?d]
 [(.getDate ?born-2) ?d]
 [(< ?name-1 ?name-2)]] db-val)

;;Two people are friends if they have worked together
;;this query uses a rule to find the friends of the person specified
(def friend-rule '[[(friends ?p1 ?p2)
  [?m :hospital/staff ?p1]
  [?m :hospital/staff ?p2]
  [(not= ?p1 ?p2)]]
 [(friends ?p1 ?p2) [?m :hospital/staff ?p1] [?m :hospital/prescriber ?p2]]
 [(friends ?p1 ?p2) (friends ?p2 ?p1)]])

(d/q '[:find ?friend
 :in $ % ?name
 :where
 [?p1 :person/name ?name]
 (friends ?p1 ?p2)
       [?p2 :person/name ?friend]] db-val friend-rule  "Nancy Pance")

;;this rule defines hospital affiliates
(def affiliate-rule '[[(affiliates ?m1 ?m2) [?m1 :hospital/affiliate ?m2]]
                  [(affiliates ?m1 ?m2) [?m :hospital/affiliate ?m2] (affiliates ?m1 ?m)]])

;;this query uses that rule to find the affiliates of a given hospital
(d/q '[:find ?affiliate
 :in $ % ?title
 :where
 [?m :hospital/title ?title]
 (affiliates ?m ?s)
 [?s :hospital/title ?affiliate]] db-val affiliate-rule "Level One Health")

;;these queries shows the count of dead people in the database
(d/q '[:find (count ?p)
       :in $
       :where [?p :person/death]] db-val)

;;find the most recent death
(d/q '[:find (max ?date)
       :in $
       :where [_ :person/death ?date]] db-val)

;;get people and their meds
(d/q '[:find ?name ?drug-name
       :in $
       :where [?e :prescription/patient ?p]
       [?p :person/name ?name]
       [?e :prescription/medication ?med-id]
       [?med-id :drug/name ?drug-name]] db-val)

;;get attributes of a given hospital
(d/q '[:find ?attr
 :in $ ?title
 :where
 [?m :hospital/title ?title]
 [?m ?a]
 [?a :db/ident ?attr]] db-val "Level One Health")

;;find every associated with a hospital
(d/q '[:find ?name
 :in $ ?title [?attr ...]
 :where
 [?m :hospital/title ?title]
 [?m ?attr ?p]
       [?p :person/name ?name]] db-val "Level One Health" [:hospital/staff :hospital/prescriber])


;;what's the schema of our database?
(d/q '[:find ?attr ?type ?card
 :where
 [_ :db.install/attribute ?a]
 [?a :db/valueType ?t]
 [?a :db/cardinality ?c]
 [?a :db/ident ?attr]
 [?t :db/ident ?type]
       [?c :db/ident ?card]] db-val)

;;wow, how about just the attributes?
(d/q '[:find ?attr
 :where
       [_ :db.install/attribute ?a]
       [?a :db/ident ?attr]] db-val)

;;when was Level One Health added to the database
(d/q '[:find ?inst
 :where
 [_ :hospital/title "Level One Health" ?tx]
       [?tx :db/txInstant ?inst]] db-val)

;;who is the prescriber for Nancy Pance?
(d/q '[:find ?name
 :where
 [?p :person/name "Nancy Pance"]
 [?m :hospital/staff ?p]
 [?m :hospital/prescriber ?d]
 [?d :person/name ?name]] db-val)

;;who is the prescriber at Level One Health?
(d/q '[:find ?name
 :where
 [?t :hospital/title "Level One Health"]
 [?t :hospital/prescriber ?p]
       [?p :person/name ?name]] db-val)
