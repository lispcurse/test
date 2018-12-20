(ns lispcurse.test-test
  (:require [lispcurse.test :refer :all]))

(deftest a-test
  "an alternative name for this test"
  {:tags [:unit]}
  [state]
  {:state state})

(deftest a-failing-test
  [state]
  (throw (AssertionError. "this test always fails")))

(deftest an-erroring-test
  [state]
  (throw (Exception. "this test always errors")))

(= (:doc (meta #'a-test)) "an alternative name for this test")
(= (:tags (meta #'a-test)) [:unit])
(= (:type (meta #'a-test)) :test)

(deftest an-old-test
  {:foo :bar})

(deftest a-poorly-written-test [foo bar]
  (println))

(run-plan {:suites [{:ns (str *ns*)
                     :tests [#'a-test
                             #'an-old-test
                             #'a-failing-test
                             #'an-erroring-test]}]})
