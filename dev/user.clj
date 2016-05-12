(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pp]
            [functional-vaadin.core :refer :all]
            [functional-vaadin.event-handling :refer :all]
            [functional-vaadin.build-support :refer :all]
            [functional-vaadin.rx.observers :as obs]
            [functional-vaadin.rx.operators :as ops]
            [functional-vaadin.utils :as u]
            [functional-vaadin.examples.run :refer [run-jetty]]
            [rx.lang.clojure.core :as rx])
  (:use clojure.test functional-vaadin.ui.test-ui-def config-gen)
  (:import (java.io File)
           (org.apache.commons.io FileUtils)
           (com.vaadin.ui UI)))

(def show-t true)
(comment
  (gen-config-table)
  (def timer (Observable/interval 1 TimeUnit/SECONDS) )
  (def ss (rx/subscribe timer (fn [t] (if show-t (println t)))))
  (def show-t false)
  (rx/unsubscribe ss)
  )

(comment
  (def server (run-jetty "functional_vaadin.examples.Sampler" true))
  (.stop server) (def server (run-jetty "functional_vaadin.examples.Sampler" true))
  (def server (run-jetty "functional_vaadin.ui.TestUI" true))
  (.stop server) (def server (run-jetty "functional_vaadin.ui.TestUI" true))
  )

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
  (def fm (form (button)))
  (def btn (.getComponent fm 0))
  (rx/subscribe (->> (obs/button-clicks btn)
                     (ops/commit)
                     ) (fn [v] (pp/pprintln v)))
  (def o (obs/events-in
           (fn [s end]
             (println "Started " end)
             (loop [i 0]
               (when (< i end)
                 (.onNext s i)
                 (Thread/sleep 1000)
                 (recur (inc i))))) 20))
  (def sub (rx/subscribe o (fn [v] (println v))))
  (rx/unsubscribed? sub)
  )
