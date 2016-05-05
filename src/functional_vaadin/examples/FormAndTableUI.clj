(ns functional-vaadin.examples.FormAndTableUI
  "A simple UI that presents a form and a table. The table can be filed by filling in the form and clicking 'Save'"
  (:use functional-vaadin.core
        functional-vaadin.naming
        functional-vaadin.utils)
  (:gen-class :name functional_vaadin.examples.FormAndTableUI
              :extends com.vaadin.ui.UI
              :main false)
  (:import (com.vaadin.ui VerticalLayout Button Table)
           (com.vaadin.data.fieldgroup FieldGroup)
           (com.vaadin.annotations Theme)))

(defn -init [ui request]
  (defui
    ui
    (panel
      "Main Panel"
      (horizontal-layout
        {:margin true}
        (form {:content VerticalLayout :id "form" :margin true}
              (form-layout
                (text-field "first-name")
                (text-field "last-name"))
              (horizontal-layout
                ;{:margin true :spacing true}
                (button
                  {:caption "Save"
                   :onClick (fn [^Button button evt ^FieldGroup fg]
                              (.commit fg)
                              (let [ui (.getUI button)
                                    ^Table table (componentAt ui :table)
                                    data-source (.getItemDataSource fg)]
                                (.addItem table (object-array
                                                  (map #(.getValue (.getItemProperty data-source %1))
                                                       ["first-name" "last-name"]))
                                          nil))
                              )})))

        (table {:caption "People" :id "table"}
               (table-column "first-name" {:header "First Name"})
               (table-column "last-name" {:header "Last Name"})
               )

        )
      )
    )
  )
