(ns functional-vaadin.examples.run
  "A main function namespace to run examples in a jetty container"
  (:require [clojure.string :as str])
  (:import (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.servlet ServletContextHandler)
           [com.vaadin.server VaadinServlet])
  (:gen-class))

(defn- jetty-server [port ui-name]
  (doto (Server. port)
    (.setHandler
      (doto (ServletContextHandler. ServletContextHandler/SESSIONS)
        (.setContextPath "/")

        (.setInitParameter "UI" ui-name)
        (.setInitParameter "legacyPropertyToString" "true")
        (.setResourceBase "dev-resources/public")
        (.addServlet VaadinServlet "/*")))))

(defn run-jetty [ui-name bg?]
  (let [server (jetty-server 8080 ui-name)]
    (.start server)
    (if bg?
      server
      (.join server))))

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
        :else (recur server)
           )))
  )

(def examples ["Sampler"])

(defn prompt-loop []
  (let [item (choose-example examples)]
    (run-and-wait item))
  (recur))

(defn -main [& args]
  (if (zero? (count args))
    (prompt-loop)
    (run-and-wait (first args))))

