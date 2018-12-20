(ns lispcurse.test)

(defmacro deftest
  [name & targs]
  `(do (defn ~name ~@targs)
       (alter-meta! (var ~name) assoc :type :test)))

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
  (when (seq suites)
    (report-all reporters {:type :begin-test-run})
    (apply-middleware middleware
                      (run-suites suites reporters)
                      {})
    (report-all reporters {:type :end-test-run})))
