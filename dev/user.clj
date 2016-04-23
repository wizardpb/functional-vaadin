(ns user
  (:use [functional-vaadin.core]
        [clojure.test]
        [config-gen]))

(def test-files ["config" "core" "utils"])

(defn run-my-tests []
  (apply run-tests (map #(symbol (str "functional-vaadin." %1 "-test")) test-files)))