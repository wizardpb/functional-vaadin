(ns functional-vaadin.examples.run
  "A main function namespace to run examples in a jetty container"
  (:require [clojure.string :as str])
  (:import (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.servlet ServletContextHandler)
           [com.vaadin.server VaadinServlet])
  (:gen-class))

(defn run-jetty [ui-name bg?]
  (let [server (Server. 8080)
        ^ServletContextHandler context (ServletContextHandler. ServletContextHandler/SESSIONS)]
    (.setContextPath context "/")
    (.setInitParameter context "UI" ui-name)
    (.setInitParameter context "legacyPropertyToString" "true")
    (.setResourceBase context "dev-resources/public")

    (.setHandler server context)
    (.addServlet context VaadinServlet "/*")

    (.start server)
    (if bg?
      server
      (.join server)
      )))

(defn choose-example [examples]
  (loop []
    (doseq [f (map-indexed #(str (inc %1) ". " %2) examples)]
      (println "  " f))
    (print "Choice? (Cntrl-C to exit) ") (flush)
    (let [item-number (dec (Integer/parseInt (read-line)))]
      (if (< item-number (count examples))
        (nth examples item-number)
        (recur))))
  )

(defn run-example [name prompt]
  (println prompt)
  (run-jetty (str "functional_vaadin.examples." name ) true))

(defn run-and-wait [name]
  (loop [server (run-example name (str "Running " name ". Type \"s\" to stop, \"r\" to restart"))]
    (let [response (.toLowerCase (read-line))]
      (cond
        (= response "s") (.stop server)
        (= response "r") (do
                           (.stop server)
                           (recur (run-example name "Restarting...")))
        true (recur server)
           )))
  )

(defn prompt-loop [examples-dir]
  (loop []
    (let [files ["Sampler"]]
     (let [item (choose-example files)]
       (run-and-wait item)))
    (recur)))

(defn -main
  ([] (prompt-loop nil))
  ([examples-dir] (prompt-loop examples-dir)))

