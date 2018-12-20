(ns lispcurse.test-test
  (:require [lispcurse.test :refer :all]))

(deftest a-test
  [state]
  {:state state})

(run-plan {:suites [{:ns (str *ns*)
                     :tests [#'a-test]}]})
