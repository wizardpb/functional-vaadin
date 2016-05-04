(ns functional-vaadin.ui.test-ui-def
  (:use functional-vaadin.core
        functional-vaadin.ui.IUIDataStore
        functional-vaadin.utils)
  (:import (com.vaadin.data.util ObjectProperty PropertysetItem)
           (com.vaadin.ui VerticalLayout Button$ClickEvent Button Table)
           (com.vaadin.data.fieldgroup FieldGroup)))


(defn define-test-ui [main-ui]
  (defui
    main-ui
    (panel
      "Main Panel"
      (horizontal-layout
        {:margin true}
        (form {:content VerticalLayout :id "form"}
              (form-layout
                (text-field "first-name")
                (text-field "last-name"))
              (horizontal-layout {:margin true :spacing true}
                                 (button {:caption "Save"
                                          :onClick (fn [^Button button evt fg]
                                                     (.commit fg)
                                                     (let [ui (.getUI button)
                                                           ^Table table (componentAt ui :table)
                                                           data-source (.getItemDataSource fg)]
                                                       (.addItem table (object-array
                                                                         (map #(.getValue (.getItemProperty data-source %1))
                                                                              ["first-name" "last-name"]))
                                                                 nil))
                                                     )})))

        (vertical-layout
          {:margin true :spacing true}
          (table {:caption "People"  :id "table"}
                 (table-column "first-name" {:header "First Name"})
                 (table-column "last-name" {:header "Last Name"})
                 )
          )
        )
      )
    )
  )
