(ns user
  (:require [clojure.string :as str])
  (:use [clojure.test]
        [functional-vaadin.core]
        [functional-vaadin.config]
        [functional-vaadin.builders]
        [functional-vaadin.data-map]
        [functional-vaadin.event-handling]
        [functional-vaadin.test-ui]
        [config-gen])
  (:import (java.io File)
           (com.vaadin.ui Component)
           (com.vaadin.server AbstractClientConnector)
           (com.vaadin.event MouseEvents$ClickEvent)))

(def test-dir "test/functional_vaadin/")

(defn test-ns-sym [fname]
  (symbol (str "functional-vaadin." (str/replace (first (str/split fname #"\.")) #"_" "-"))))

(defn run-my-tests []
  (let [test-files (.list (File. ^String test-dir))]
    (doseq [fname test-files]
      (load-file (str test-dir fname)))
    (apply run-tests (map test-ns-sym test-files))))

(def dp (->UIDataProvider {}))

