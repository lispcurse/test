(ns lispcurse.test
  (:require [lispcurse.reporters.default :as default]))

;; Assertions

(defmacro assert-expr
  [msg form])

(defmacro is
  ([form] `(is ~form nil))
  ([form msg] `(assert-expr ~msg ~form)))


;; Test DSL

(defmacro deftest
  [name & tdecl]
  (assert (symbol? name) "First argument of deftest must be a symbol")
  (let [[m tdecl] (if (string? (first tdecl))
                    [{:doc (first tdecl)} (rest tdecl)]
                    [{} tdecl])
        [m tdecl] (if (map? (first tdecl))
                    [(conj m (first tdecl)) (rest tdecl)]
                    [m tdecl])
        [args tdecl] (if (vector? (first tdecl))
                       [(first tdecl) (rest tdecl)]
                       ['[state] tdecl])]
    (assert (= (count args) 1) "deftest only takes a single argument")
    `(defn ~name
       ~(assoc m :type :test)
       ~args
       (try
         ~@tdecl
         {:type :pass
          :state ~(first args)
          :meta ~m}
         (catch AssertionError ae#
           {:type :fail
            :failure ae#
            :state ~(first args)
            :meta ~m})
         (catch Throwable t#
           {:type :error
            :error t#
            :state ~(first args)
            :meta ~m})))))

;; Test Running

(defn report-all
  [reporters event]
  (doseq [reporter reporters]
    (reporter event)))

(defn apply-middleware
  [middleware-fns handler state]
  (let [middleware (apply comp middleware-fns)]
    ((middleware handler) state)))

(defn run-tests
  [tests reporters]
  (fn [suite-state]
    (doseq [test-var tests]
      (report-all reporters {:type :begin-test})
      (let [test-middleware (:middleware (meta test-var))
            test-result (apply-middleware test-middleware test-var suite-state)]
        (report-all reporters test-result)
        (report-all reporters {:type :end-test})
        test-result))))

(defn run-suites
  [suites reporters]
  (fn [global-state]
    (doseq [{:keys [ns tests] :as suite} suites]
      (report-all reporters {:type :begin-test-suite
                             :ns ns})
      (let [suite-result (apply-middleware (:middleware suite)
                                           (run-tests tests reporters)
                                           global-state)]
        (report-all reporters {:type :end-test-suite
                               :ns ns})
        suite-result))))

(defn run-plan
  [{:keys [suites middleware reporters] :as test-plan}]
  (let [reporters (if (seq reporters)
                    reporters
                    [default/reporter])]
    (when (seq suites)
      (report-all reporters {:type :begin-test-run})
      (apply-middleware middleware
                        (run-suites suites reporters)
                        {})
      (report-all reporters {:type :end-test-run}))))

#_{:suites [{:ns (str *ns*)
             :middleware [adds-suite]
             :tests [#'my-unit-test
                     #'my-other-unit-test]}]
   :middleware [adds-global]
   :reporters [(fn [event]
                 (println event))]}
