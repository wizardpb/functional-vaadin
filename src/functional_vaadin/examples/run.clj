(ns functional-vaadin.examples.run
  "A main function namespace to run examples in a jetty container"
  (:require [clojure.string :as str])
  (:import (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.servlet DefaultServlet ServletContextHandler)
           [com.vaadin.server VaadinServlet]
           (java.io File))  )

(defn run-jetty [ui-name bg?]
  (let [server (Server. 8080)
        ^ServletContextHandler context (ServletContextHandler. ServletContextHandler/SESSIONS)]
    (.setContextPath context "/")
    (.setInitParameter context "UI" ui-name)
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
    (doseq [f (map-indexed #(str (inc %1) ". " (first (str/split %2 #"\."))) examples)]
      (println "  " f))
    (print "Choice? (Cntrl-C to exit) ") (flush)
    (let [item (dec (Integer/parseInt (read-line)))]
      (if (< item (count examples))
        (nth examples item)
        (recur))))
  )

(defn run-example [name prompt]
  (println prompt)
  (run-jetty (str "functional-vaadin.examples." name ) true))

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
    (let [files (filter
                 #(re-matches #"[A-Z].*\.clj" %1)
                 (.list (File. (or examples-dir "src/functional_vaadin/examples"))))]
     (let [item (choose-example files)]
       (run-and-wait item)))
    (recur)))

(defn -main
  ([] (prompt-loop nil))
  ([examples-dir] (prompt-loop examples-dir)))
