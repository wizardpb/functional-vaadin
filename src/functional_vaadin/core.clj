(ns functional-vaadin.core
  "The primary namespace for the project, contains all publically accesible buidlers and vars. This should be the only
  required namespace for use in a project"
  (:require [clojure.set :as set]
            [functional-vaadin.naming]
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
             Component UI Field Image ProgressBar)
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
  "Create a Button component from constructor arguments or a configuration Map"
  [& args]
  (first (create-widget Button args false)))

(defn link
  "Create a Link component from constructor arguments or a single configuration Map"
  [& args]
  (first (create-widget Link args false)))

(defn label
  "Create a Label component from constructor arguments or a configuration Map"
  [& args]
  (first (create-widget Label args false)))

;; Fields

; TODO - add explicit bind option to specify form bindng, with initial value (type implied from initial value) or explicit type
; TODO - field validation

(defn text-field
  "Create a TextField component from constructor arguments or a configuration Map.
  When used inside a form, will take an extra intial argument that
  defined the data bindng property the field will bind to"
  [& args]
  (create-field TextField args))

(defn password-field
  "Create a PassowrdField component from constructor arguments or a configuration Map.
  When used inside a form, will take an extra intial argument that
  defined the data bindng property the field will bind to"
  [& args]
  (create-field PasswordField args))

(defn text-area
  "Create a TextArea component from constructor arguments or a configuration Map.
  When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field TextArea args))

(defn rich-text-area
  "Create a RichTextArea component from constructor arguments or a configuration Map.
  When used inside a form, will take an extra intial argument that
  defined the data bindng property the field will bind to"
  [& args]
  (create-field RichTextArea args))

(defn inline-date-field
  "Create a InineDateField component from constructor arguments or a configuration Map.
  When used inside a form, will take an extra intial argument that
  defined the data bindng property the field will bind to"
  [& args]
  (create-field InlineDateField args))

(defn popup-date-field
  "Create a PopupDateField component. When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field PopupDateField args))

(defn slider
  "Create a Slider component from constructor arguments or a configuration Map.
  When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field Slider args))

(defn check-box
  "Create a CheckBox component from constructor arguments or a configuration Map.
  When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field CheckBox args))

(defn combo-box
  "Create a ComboBox component from constructor arguments or a configuration Map.
  When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field ComboBox args))

(defn twin-col-select
  "Create a TwinColSelect component from constructor arguments or a configuration Map.
  When used inside a form, will take an extra intial argument that
defined the data bindng property the field will bind to"
  [& args]
  (create-field TwinColSelect args))

(defn native-select
  "Create a NativeSelect component from constructor arguments or a configuration Map.
  When used inside a form, will take an extra intial argument that defined the data bindng property the field will bind to"
  [& args]
  (create-field NativeSelect args))

(defn list-select
  "Create a ListSelect component from constructor arguments or a configuration Map. When used inside a form, will
  take an extra intial argument that defined the data bindng property the field will bind to"
  [& args]
  (create-field ListSelect args))

(defn option-group
  "Create an OptionGroup component from constructor arguments or a configuration Map. When used inside a form,
  will take an extra intial argument that defined the data bindng property the field will bind to"
  [& args]
  (create-field OptionGroup args))

(defn progress-bar
  "Create a ProgressBar component from constructor arguments or a configuration Map. When used inside a form, will take
  an extra intial argument that\n  defined the data bindng property the field will bind to"
  [& args]
  (create-field ProgressBar args))

;; Containers and layouts

(defn panel
  "Create a Panel component from constructor arguments or a configuration Map. Allows only a single child which will be set as the content" [& args]
  (let [[panel children] (create-widget Panel args true)]
    (add-children panel children)))

(defn vertical-layout
  "Create a VerticalLayout component from constructor arguments or a configuration Map. Remaining arguments are children.
  Expansion ration and alignment parameters are placed on the children,
  not the layout itself"
  [& args]
  (let [[vl children] (create-widget VerticalLayout args true)]
    (add-children vl children)))

(defn horizontal-layout
  "Create a HorizontalLayout component from constructor arguments or a configuration Map. Remaining arguments are children.
  Expansion ration a alignment parameters  areplaced on the children,
not the layout itself"
  [& args]
  (let [[hl children] (create-widget HorizontalLayout args true)]
    (add-children hl children)))

(defn form-layout
  "Create a FormLayout component from constructor arguments or a configuration Map. Remaining arguments are children.."
  [& args]
  (let [[hl children] (create-widget FormLayout args true)]
    (add-children hl children)))

(defn grid-layout
  "Create a GridLayout component from constructor arguments or a configuration Map. Remaining arguments are children.
  Childen may have :position and :span configuration options to specify their position and size"
  [& args]
  (let [[hl children] (create-widget GridLayout args true)]
    (add-children hl children)))

(defn tab-sheet
  "Create a TabSheet componentfrom constructor arguments or a configuration Map. Remaining arguments are children.."
  [& args]
  (let [[ts children] (create-widget TabSheet args true)]
    (add-children ts children)))

(defn vertical-split-panel
  "Create a VerticalSplitPanel component from constructor arguments or a configuration Map. Remaining arguments are children."
  [& args]
  (let [[sl children] (create-widget VerticalSplitPanel args true)]
    (add-children sl children)))

(defn horizontal-split-panel
  "Create a HorizontalSplitPanel component from constructor arguments or a configuration Map. Remaining arguments are children."
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
     (let [[l# c#] (create-form-content (list ~@args))]
       (add-children l# c#)
       (set-field-group l# *current-field-group*)
       l#)))

;; Embedded items

(defn image
  "Create an Image component from constructor arguments or a configuration Map."
  [& args]
  (first (create-widget Image args false)))

(defn embedded
  "Create an Embedded component from constructor arguments or a configuration Map."
  [& args]
  (first (create-widget Embedded args false)))

;; Tables

(defn table-column
  "Create a table column. Only valid as a child of a Table component. The first argument must be the name of the
  data binding property that the column will bind to. Other config options can be the type (:type) and default value
  (:default) of the property, plus any of the table setColumnXXX setters. These will be configured on the table as
  for other config options"
  ([propertyId config]
   (assoc
     (merge {:type Object :defaultValue nil} config)
     :propertyId propertyId)
   )
  ([propertyId] (table-column propertyId {}))
  )

(defn table
  "Create a Table component from constructor arguments or a configuration Map. Children must be table-column specifications"
  [& args]
  (let [[table children] (create-widget Table args true)]
    (add-children table children)))


