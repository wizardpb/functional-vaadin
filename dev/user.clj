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
  (:import (java.io File)
           (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.servlet DefaultServlet ServletContextHandler)
           [com.vaadin.server VaadinServlet]))

(def test-dir "test/functional_vaadin/")

(defn test-ns-sym [fname]
  (symbol (str "functional-vaadin." (str/replace (first (str/split fname #"\.")) #"_" "-"))))

(defn run-my-tests []
  (let [test-files (.list (File. ^String test-dir))]
    (doseq [fname test-files]
      (load-file (str test-dir fname)))
    (apply run-tests (map test-ns-sym test-files))))

(def dp (->UIDataProvider {} nil))

(defn run-jetty []
  (let [server (Server. 8080)
        ^ServletContextHandler context (ServletContextHandler. ServletContextHandler/SESSIONS)]
    (.setContextPath context "/")
    (.setInitParameter context "UI" "functional_vaadin.TestUI")
    (.setResourceBase context "dev-resources/public")

    (.setHandler server context)
    (.addServlet context VaadinServlet "/*")

    (.start server)
    server
    ;(.join server)
    )
  )

(defn define-test-ui [main-ui]
  (defui
    main-ui
    (panel
      "Main Panel"
      (vertical-layout
        {:margin true}
        (tab-sheet
          (vertical-layout
            {:caption "Vertical" :margin true :spacing true}
            (label "Line1") (label "Line2")
            )
          (horizontal-layout
            {:caption "Horizontal" :margin true :spacing true}
            (label "Left") (label "Right")
            )
          (grid-layout
            {:caption "Grid" :rows 2 :columns 2 :margin true}
            (label "11") (label "21")
            (label "11") (label "21")
            )
          )
        )
      )
    )
  )
