(ns functional-vaadin.examples.Sampler
  "A simple UI that presents some UI examples in a TabSheet: a form and a table. progress bar, etc..
  The table can be filed by filling in the form and clicking 'Save'"
  (:use functional-vaadin.core
        functional-vaadin.naming
        functional-vaadin.rx.observers
        functional-vaadin.rx.operators
        functional-vaadin.utils)
  (:gen-class :name ^{com.vaadin.annotations.Theme "valo"} functional_vaadin.examples.Sampler
              :extends com.vaadin.ui.UI
              :main false)
  (:import (com.vaadin.ui VerticalLayout Button Table)
           (com.vaadin.data.fieldgroup FieldGroup)
           (com.vaadin.annotations Theme)))

(defn -init [main-ui request]
  (defui
    main-ui
    (panel
      "Sampler"
      (tab-sheet
        (horizontal-layout
          {:sizeFull [] :caption "Form and Table"}
          (form {:content VerticalLayout :id :form :margin true :sizeFull []}
                (form-layout
                  (text-field "first-name" String)
                  (text-field "last-name" String))
                (horizontal-layout
                  (button {:caption "Save" :id :save-button}))
                )
          (vertical-layout
            {:margin true :sizeFull []}
            (table {:caption "People" :sizeFull [] :id :table}
                   (table-column "first-name" {:header "First Name" :width 200})
                   (table-column "last-name" {:header "Last Name"})
                   )
            )
          )
        (vertical-layout
          {:caption "Background Task"}
          (horizontal-layout
            {:margin true :spacing true}
            (button {:caption "Start" :id :start-button})
            (button {:caption "Stop" :id :stop-button}))
          )
        )
      )
    )
  (->> (buttonClicks (componentAt main-ui :save-button))
       (commit)
       (consume-for (componentAt main-ui :table)
                    (fn [table data]
                      (let [{:keys [item]} data
                            row (object-array
                                  (map #(.getValue (.getItemProperty item %1))
                                       ["first-name" "last-name"]))]
                        (.addItem table row nil))
                      )))

  )
