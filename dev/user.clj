(ns user
  (:require [clojure.string :as str]
            [functional-vaadin.utils :as u])
  (:use clojure.test
        functional-vaadin.ui.IUIDataStore
        functional-vaadin.ui.TestUI
        functional-vaadin.ui.test-ui-def
        config-gen)
  (:import (functional_vaadin.ui TestUI)
           (java.io File)
           (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.servlet DefaultServlet ServletContextHandler)
           (org.apache.commons.io FileUtils)
           [com.vaadin.server VaadinServlet]
           (com.vaadin.ui Button Label)
           ))

(declare run-jetty)

(comment
  (def server (run-jetty))
  (do (.stop server) (def server (run-jetty)))
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

(defn run-jetty []
  (let [server (Server. 8080)
        ^ServletContextHandler context (ServletContextHandler. ServletContextHandler/SESSIONS)]
    (.setContextPath context "/")
    (.setInitParameter context "UI" "functional_vaadin.ui.TestUI")
    (.setResourceBase context "dev-resources/public")

    (.setHandler server context)
    (.addServlet context VaadinServlet "/*")

    (.start server)
    server
    ;(.join server)
    ))


