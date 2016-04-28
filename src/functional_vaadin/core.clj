(ns functional-vaadin.core
  "Definition of all user functions for the libray - UI definition macro and all functions to build
  individual Vaadinwidgets"
  (:use [functional-vaadin.config]
    )

  (:import (com.vaadin.ui
             MenuBar
             Label Embedded Link Upload Button Calendar
             Panel VerticalLayout HorizontalLayout FormLayout GridLayout TabSheet VerticalSplitPanel HorizontalSplitPanel
             TextField TextArea PasswordField RichTextArea InlineDateField PopupDateField Slider CheckBox ComboBox TwinColSelect NativeSelect ListSelect OptionGroup Table Tree TreeTable
             Component UI Field)
           (java.util Date Map)
           (com.vaadin.data.fieldgroup FieldGroup)
    ;(javax.servlet.http HttpSessionBindingListener)
           ))

(defprotocol IUIData
  "A protocol to assign and lookup components by ID, and provide a shared UI data area
  addressable by hierarchical (dot-separated) symbols"
  (add-component [this component id] "Assign an ID")
  (component-at [this component-id] "Look up a component")
  (set-data-source [this component source-id] "Tell a component where to get data")
  (set-data-at [this source-id data] "Set a dat avalue for a source")
  (get-data-at [this component-id] "Get data from a component"))

(defmacro defui [^UI ui top-form]
  `(let [this-ui# ~ui]
     (with-bindings
      {#'*current-ui* this-ui#}
      (let [root# ~top-form]
        (if (instance? Component root#)
          (.setContent *current-ui* root#)
          (throw
            (UnsupportedOperationException. "The generated UI is not a Vaadin Component"))))
      )
     this-ui#))

