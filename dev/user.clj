(ns user
  (:require [clojure.string :as str]
            [clojure.pprint :as pp]
            [functional-vaadin.core :refer :all]
            [functional-vaadin.event-handling :refer :all]
            [functional-vaadin.build-support :refer :all]
            [functional-vaadin.rx.observers :as obs]
            [functional-vaadin.rx.operators :as ops]
            [functional-vaadin.utils :as u]
            [rx.lang.clojure.core :as rx])
  (:use clojure.test
        functional-vaadin.ui.test-ui-def
        config-gen
        )
  (:import (java.io File)
           (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.servlet DefaultServlet ServletContextHandler)
           (org.apache.commons.io FileUtils)
           [com.vaadin.server VaadinServlet]
           (com.vaadin.ui Button Label)
           ))

(def show-t true)
(comment
  (gen-config-table)
  (def timer (Observable/interval 1 TimeUnit/SECONDS) )
  (def ss (rx/subscribe timer (fn [t] (if show-t (println t)))))
  (def show-t false)
  (rx/unsubscribe ss)
  )

(declare run-jetty)

(comment
  (def server (run-jetty "functional_vaadin.examples.Sampler"))
  (.stop server) (def server (run-jetty "functional_vaadin.examples.Sampler"))
  (def server (run-jetty "functional_vaadin.ui.TestUI"))
  (.stop server) (def server (run-jetty "functional_vaadin.ui.TestUI"))
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

(defn run-jetty [ui-name]
  (let [server (Server. 8080)
        ^ServletContextHandler context (ServletContextHandler. ServletContextHandler/SESSIONS)]
    (.setContextPath context "/")
    (.setInitParameter context "UI" ui-name)
    (.setResourceBase context "dev-resources/public")

    (.setHandler server context)
    (.addServlet context VaadinServlet "/*")

    (.start server)
    server
    ;(.join server)
    ))

(comment
  (def fm (form (button)))
  (def btn (.getComponent fm 0))
  (rx/subscribe (->> (obs/buttonClicks btn)
                     (ops/commit)
                     ) (fn [v] (pp/pprintln v)))
  )
