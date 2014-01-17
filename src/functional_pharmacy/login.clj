(ns functional-pharmacy.login)

(def patients ["bob"
               "sue"
               "jill"
               "stu"])

(defn contains-name [s]
  (some #(= s %) patients))

(defn login-as
  [username]
  (if (contains-name username)
    (ns functional-pharmacy.patient-actions)))