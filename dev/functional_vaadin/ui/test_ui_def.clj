(ns functional-vaadin.ui.test-ui-def
  (:use functional-vaadin.core
        functional-vaadin.utils)
  (:import (com.vaadin.data.util ObjectProperty PropertysetItem)
           (com.vaadin.ui VerticalLayout)
           (com.vaadin.data.fieldgroup FieldGroup)))

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
