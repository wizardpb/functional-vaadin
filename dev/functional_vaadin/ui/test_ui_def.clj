(ns functional-vaadin.ui.test-ui-def
  (:use functional-vaadin.core
        functional-vaadin.ui.IUIDataStore
        functional-vaadin.utils)
  (:import (com.vaadin.data.util ObjectProperty PropertysetItem)
           (com.vaadin.ui VerticalLayout)
           (com.vaadin.data.fieldgroup FieldGroup)))

(defn saveClicked [evt fg]
  (.commit fg)
  (let [button (.getSource evt)
        ui (.getUI button)
        formData (getBindingValue ui :form.data)]
    (updateBinding ui
                   :table.data
                   (fn [old]
                     (conj (or old []) formData)))
    )
  )

(defn define-test-ui [main-ui]
  (defui
    main-ui
    (panel
      "Main Panel"
      (horizontal-layout
        {:margin true}
        (form {:content VerticalLayout :id "form" :bind :form.data}
              (form-layout
                (text-field "first-name")
                (text-field "last-name"))
              (horizontal-layout {:margin true :spacing true}
                                 (button {:caption "Save"
                                          :onClick saveClicked})))

        (vertical-layout
          {:margin true :spacing true}
          (table "People" {:id "table" :bind :table.data}
                 (table-column "first-name" {:header "First Name"})
                 (table-column "last-name" {:header "Last Name"})
                 )
          )
        )
      )
    )
  (updateBinding main-ui :form.data
                 (fn [old-value]
                   {"first-name" "Paul" "last-name" "Bennett"}))
  )
