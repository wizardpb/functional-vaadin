(ns user
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [functional-vaadin.core :refer :all]
            ;[functional-vaadin.config :refer :all]
            [functional-vaadin.builders :refer :all]
            ;[functional-vaadin.data-map :refer :all]
            ;[functional-vaadin.event-handling :refer :all]
            [functional-vaadin.mock-data-provider :refer :all]
            [config-gen :refer :all])
  (:import (java.io File)))

(def test-dir "test/functional_vaadin/")

(defn test-ns-sym [fname]
  (symbol (str "functional-vaadin." (str/replace (first (str/split fname #"\.")) #"_" "-"))))

(defn run-my-tests []
  (let [test-files (.list (File. ^String test-dir))]
    (doseq [fname test-files]
      (load-file (str test-dir fname)))
    (apply run-tests (map test-ns-sym test-files))))

(def dp (->UIDataProvider {} nil))

