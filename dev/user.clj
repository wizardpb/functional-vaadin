(ns user
  (:require [clojure.string :as str])
  (:use [clojure.test]
        [functional-vaadin.core]
        [config-gen])
  (:import (java.io File)))

(def test-dir "test/functional_vaadin/")

(defn test-ns-sym [fname]
  (symbol (str "functional-vaadin." (str/replace (first (str/split fname #"\.")) #"_" "-"))))

(defn run-my-tests []
  (let [test-files (.list (File. ^String test-dir))]
    (doseq [fname test-files]
      (load-file (str test-dir fname)))
    (apply run-tests (map test-ns-sym test-files))))