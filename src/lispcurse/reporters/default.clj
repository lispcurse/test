(ns lispcurse.reporters.default)

(defmulti reporter :type)

(defmethod reporter :begin-test [event])
(defmethod reporter :end-test [event])

(defmethod reporter :begin-test-run [event]
  (println "Starting to run some tests"))
(defmethod reporter :end-test-run [event]
  (println "Ran some tests!"))

(defmethod reporter :begin-test-suite [event]
  (println "Testing" (:ns event)))
(defmethod reporter :end-test-suite [event])

(defmethod reporter :default [event]
  (println event))

