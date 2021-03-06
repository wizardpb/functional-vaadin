;;
;; Copyright 2016 Prajna Inc. All Rights Reserved.
;;
;; This work is licensed under the Eclipse Public License v1.0 - https://www.eclipse.org/legal/epl-v10.html
;; Distrubition and use must be done under the terms of this license
;;

(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pp]
            [clojure.spec.alpha :as s]
            [functional-vaadin.core :refer :all]
            [functional-vaadin.event-handling :refer :all]
            [functional-vaadin.build-support :refer :all]
            [functional-vaadin.rx.observers :as obs]
            [functional-vaadin.rx.operators :as ops]
            [functional-vaadin.utils :as u]
            [functional-vaadin.examples.run :refer [run-jetty]]
            [rx.lang.clojure.core :as rx]
            [clojure.tools.namespace.repl :refer [refresh]])
  (:use clojure.test config-gen
        functional-vaadin.ui.test-ui-def
        )
  (:import (java.io File)
           (org.apache.commons.io FileUtils)
           (com.vaadin.ui UI)))

(def test-dir "test/")

(defn test-ns-sym [fname]
  (symbol (-> fname
              (str/replace #"\.clj$" "")
              (str/replace #"test/" "")
              (str/replace #"/" ".")
              (str/replace #"_" "-"))))

(defn file-paths [^String base-dir]
  (map #(.getPath %1)
       (FileUtils/listFiles
         ^File (File. base-dir)
         #^"[Ljava.lang.String;" (into-array ["clj"])
         true)))

(defn run-my-tests []
  (let [test-files (file-paths test-dir)]
    (doseq [fname test-files]
      (load-file fname))
    (apply run-tests (map test-ns-sym test-files))))


(comment
  (gen-config-table)
  (do (refresh) (run-my-tests))
  (def server (run-jetty "functional_vaadin.examples.Sampler" true))
  (do (.stop server) (def server (run-jetty "functional_vaadin.examples.Sampler" true)))
  (def server (run-jetty "functional_vaadin.ui.TestUI" true))
  (.stop server) (def server (run-jetty "functional_vaadin.ui.TestUI" true))
  (def server (run-jetty "functional_vaadin.examples.todo.ToDo" true))
  (do (.stop server) (def server (run-jetty "functional_vaadin.examples.todo.ToDo" true)))
  )

