(ns flock.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [flock.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'flock.core-test))
    0
    1))
