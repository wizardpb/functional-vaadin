(ns user
  (:require [clojure.string :as str])
  (:use clojure.test
        functional-vaadin.core
        functional-vaadin.utils
        config-gen)
  (:import (java.io File)
           (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.servlet DefaultServlet ServletContextHandler)
           (org.apache.commons.io FileUtils)
           [com.vaadin.server VaadinServlet]
           [com.vaadin.data.util ObjectProperty PropertysetItem]
           [com.vaadin.data.fieldgroup FieldGroup]
           [com.vaadin.ui VerticalLayout]
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

(declare add-file-paths)

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
    )
  )

(defn define-test-ui [main-ui]
  (defui
    main-ui
    (panel
      "Main Panel"
      (let [prop (ObjectProperty. "Hello")]
        (vertical-layout
          {:margin true}
          (tab-sheet
            (vertical-layout
              {:caption "Property Binding" :margin true :spacing true}
              (text-field {:caption "Input" :propertyDataSource prop :immediate true})
              (label {:propertyDataSource prop})
              )
            (let [item (PropertysetItem.)]
              (.addItemProperty item "first-name" (ObjectProperty. "Paul"))
              (.addItemProperty item "last-name" (ObjectProperty. "Bennett"))
              (horizontal-layout
               {:caption "Item Binding" :margin true :spacing true}
               (let [form (form {:content VerticalLayout}
                                (form-layout
                                  (text-field "first-name")
                                  (text-field "last-name"))
                                (horizontal-layout {:margin true :spacing true}
                                                   (button {:caption "Save"
                                                            :onClick (fn [evt ^FieldGroup fg] (.commit fg))})))]
                 (.setItemDataSource (get-data form :field-group) item)
                 form)

               (vertical-layout {:margin true :spacing true}
                                (label {:propertyDataSource (.getItemProperty item "first-name")})
                                (label {:propertyDataSource (.getItemProperty item "last-name")}))
               ))
            )
          ))
      )
    )
  )
