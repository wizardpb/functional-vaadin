(ns functional-vaadin.core
  (:use [functional-vaadin.build-support]
        [functional-vaadin.config])
  (:import (com.vaadin.ui
             Label Embedded Link MenuBar Upload Button Calendar GridLayout
             TabSheet VerticalSplitPanel HorizontalSplitPanel Slider TextField TextArea PasswordField CheckBox
             RichTextArea InlineDateField PopupDateField Table ComboBox TwinColSelect NativeSelect
             ListSelect OptionGroup Tree TreeTable Panel VerticalLayout HorizontalLayout FormLayout
             Component UI)))

(def
  ^{:dynamic true :private true}
  *current-ui*
  "A dynamic var that will hold the current ui during building")

(defprotocol IUIData
  "A protocol to assign and lookup components by ID, and provide a shared UI data area
  addressable by hierarchical (dot-separated) symbols"
  (add-component [this component id] "Assign an ID")
  (component-at [this id] "Look up a component")
  (set-data-source [this component source-id] "Tell a component where to get data")
  (set-data [this source-id] "Set a dat avalue for a source"))

(defmacro defui [^UI ui top-form]
  `(with-bindings
     {#'*current-ui* ~ui}
     (let [root ~top-form]
       (if (isa? root Component)
         (.setComponent *current-ui* root)
         (throw
           (UnsupportedOperationException. "The generated UI is not a Vaadin Component"))))
     *current-ui*))

(defn panel [opts & children]
  (add-children (configure (Panel.) opts) children))

(defn vertical-layout [opts & children]
  (add-children (configure (VerticalLayout.) opts) children))

(defn horizontal-layout [opts & children]
  (add-children (configure (HorizontalLayout.) opts) children))

(defn form-layout [opts & children]
  (add-children (configure (FormLayout.) opts) children))

(defn button [opts]
  (configure (Button.) opts))

(defn link [opts]
  (configure (Link.) opts))

(defn label [opt-or-text]
  (if (instance? String opt-or-text)
    (Label. opt-or-text)
    (configure (Label.) opt-or-text)))