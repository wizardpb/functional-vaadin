(ns functional-vaadin.core
  "The primary namespace for the project, contains all publically accesible buidlers and vars. This should be the only
  required namespace for use in a project"
  (:require [clojure.set :as set]
            [functional-vaadin.ui.IUIDataStore]
            [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.build-support :refer :all]
            [functional-vaadin.utils  :refer :all])
  (:import (com.vaadin.ui
             MenuBar
             Label Embedded Link Upload Button Calendar
             Panel VerticalLayout HorizontalLayout FormLayout GridLayout TabSheet VerticalSplitPanel HorizontalSplitPanel
             TextField TextArea PasswordField RichTextArea InlineDateField PopupDateField Slider CheckBox
             ComboBox TwinColSelect NativeSelect ListSelect OptionGroup
             Table Tree TreeTable
             Component UI Field Image)
           (java.util Date Map)
           (com.vaadin.data.fieldgroup FieldGroup)
           (com.vaadin.data.util PropertysetItem)))

;; TODO - Resources and conversions

;; Primary build macro

(defmacro defui
  "Defines a Vaadin UI using the builder syntax. Given a com.vaadin.ui.UI object and a series of builder forms, creates
  and installs the generated components on the UI object."
  [^UI ui top-form]
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

;; Base components - Button, Link, Label etc.

(defn button
  "Create a Button component"
  [& args]
  (first (create-widget Button args false)))

(defn link
  "Create a Link component"
  [& args]
  (first (create-widget Link args false)))

(defn label
  "Create a Label component"
  [& args]
  (first (create-widget Label args false)))

;; Forms and Fields

(defn text-field
  "Create a TextField component. When used inside a form, will take an extra intial argument that
  defined the data bindng property the field will bind to"
  [& args]
  (create-field TextField args))

(defn password-field
  "Create a PassowrdField component. When used inside a form, will take an extra intial argument that
  defined the data bindng property the field will bind to"
  [& args]
  (create-field PasswordField args))

(defn text-area
  "Create a TextArea component. When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field TextArea args))

(defn rich-text-area
  "Create a RichTextArea component. When used inside a form, will take an extra intial argument that
  defined the data bindng property the field will bind to"
  [& args]
  (create-field RichTextArea args))

(defn inline-date-field
  "Create a InineDateField component. When used inside a form, will take an extra intial argument that
  defined the data bindng property the field will bind to"
  [& args]
  (create-field InlineDateField args))

(defn popup-date-field
  "Create a PopupDateField component. When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field PopupDateField args))

(defn slider
  "Create a Slider component. When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field Slider args))

(defn check-box
  "Create a CheckBox component. When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field CheckBox args))

(defn combo-box
  "Create a ComboBox component. When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field ComboBox args))

(defn twin-col-select
  "Create a TwinColSelect component. When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field TwinColSelect args))

(defn native-select
  "Create a NativeSelect component. When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field NativeSelect args))

(defn list-select
  "Create a ListSelect component. When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field ListSelect args))

(defn option-group
  "Create an OptionGroup component. When used inside a form, will take an extra intial argument that
  defined the data bindng property the field will bind to"
  [& args]
  (create-field OptionGroup args))

;; Containers and layouts

(defn panel
  "Create a Panel component. Allows only a single child" [& args]
  (let [[panel children] (create-widget Panel args true)]
    (add-children panel children)))

(defn vertical-layout
  "Create a VerticalLayout component. Expansion ration a alignment parameters can be placed on the children,
  not the layout itself"
  [& args]
  (let [[vl children] (create-widget VerticalLayout args true)]
    (add-children vl children)))

(defn horizontal-layout
  "Create a HorizontalLayout component. Expansion ration a alignment parameters can be placed on the children,
not the layout itself"
  [& args]
  (let [[hl children] (create-widget HorizontalLayout args true)]
    (add-children hl children)))

(defn form-layout
  "Create a FormLayout component."
  [& args]
  (let [[hl children] (create-widget FormLayout args true)]
    (add-children hl children)))

(defn grid-layout
  "Create a GridLayout component."
  [& args]
  (let [[hl children] (create-widget GridLayout args true)]
    (add-children hl children)))

(defn tab-sheet
  "Create a TabSheet component."
  [& args]
  (let [[ts children] (create-widget TabSheet args true)]
    (add-children ts children)))

(defn vertical-split-panel
  "Create a VerticalSplitPanel component."
  [& args]
  (let [[sl children] (create-widget VerticalSplitPanel args true)]
    (add-children sl children)))

(defn horizontal-split-panel
  "Create a HorizontalSplitPanel component."
  [& args]
  (let [[sl children] (create-widget HorizontalSplitPanel args true)]
    (add-children sl children)))

;; Forms

(defmacro form
  "Create a Form. This is a pseudo component that creates a layout component (specified by the :content configuration
  option) and adds a controling Field Group to the layout (stored on the components 'data' attribute). This is made
  available in all event handlers attached to form components"
  [& args]
  `(with-bindings {#'*current-field-group* (FieldGroup. (PropertysetItem.))}
     (let [[l# c#] (create-form-layout (list ~@args))]
       (add-children l# c#)
       (attach-data l# :field-group *current-field-group*)
       l#)))

;; Embedded items

(defn image
  "Create an Image component."
  [& args]
  (first (create-widget Image args false)))

(defn embedded
  "Create an Embedded component."
  [& args]
  (first (create-widget Embedded args false)))

;; Tables

(def valid-column-options
      #{:propertyId :type :defaultValue :header :icon :alignment})

(defn table-column
  "Create a table column. Only valid as a child of a Table component. The first argument must be the name of the
  data binding property that the column will bind to. Other config options can be the type (:type) and default value
  (:default) of the property, the column header (:header), a Resource for the column icon (:icon) and the column
  alignment (:alignment)"
  ([^String propertyId ^Map config]
   (-> (assoc
         (merge {:type Object :defaultValue nil} config)
         :propertyId propertyId)
       (convert-column-values)
       (validate-column-options)))

  ([^String propertyId] (table-column propertyId {}))

  )

(defn table
  "Create a Table component."
  [& args]
  (let [[table children] (create-widget Table args true)]
    (add-children table children)))


