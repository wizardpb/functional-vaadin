(ns functional-vaadin.core
  "The primary namespace for the project, contains all publically accesible buidlers and vars.

  All builder functions take a variable number of orguments, all of a similar form:

  (<builder> ctor-arguments? config? & children)

  where:

  ctor-arguments: are any set of constructor arguments for the corresponding component, as defined in the Vaadin javadoc.
                  if this is not supplied, the null constructor will be used.

  config:         is a Map of attribute-vaue pairs corresponding to setters on the Component. The names are the same as the
                  setter name, minus the 'set' prefix, and all lower case e.g. the config '{:id \"this-como\"} will set
                  the 'id' attribute using setId(val)

  children:       are the Components child Components, which will be added as appropriate
                  "

  (:require [clojure.set :as set]
            [functional-vaadin.naming :as nm]
            [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.build-support :refer :all]
            [functional-vaadin.utils  :refer :all])
  (:import (com.vaadin.ui
             MenuBar
             Label Embedded Link Upload Button Calendar
             Panel VerticalLayout HorizontalLayout FormLayout GridLayout TabSheet VerticalSplitPanel HorizontalSplitPanel
             TextField TextArea PasswordField RichTextArea InlineDateField PopupDateField Slider CheckBox
             ComboBox TwinColSelect NativeSelect ListSelect OptionGroup
             Table Tree TreeTable Accordion
             Component UI Field Image ProgressBar MenuBar$MenuItem Window)
           (com.vaadin.data.fieldgroup FieldGroup)
           (com.vaadin.data.util PropertysetItem)
           (com.vaadin.event ShortcutAction Action$Listener)
           ))


; TODO - upload, calendar, popupview
; TODO - layouts: absolute, css, custom(?)
; TODO - registering custom components?

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
           (unsupported-op "The generated UI is not a Vaadin Component")))
       )
     this-ui#))

;; Public functions - named component access

(defn componentNamed
  "Return a component named with an : configuration attriute in the given UI"
  [key ui]
  (nm/componentAt ui key))

;; Auxiliary objects - actions, etc.

(defn shortcutAction
  ([name keycode a-fn modifiers]
   (proxy [ShortcutAction Action$Listener] [name (int keycode) (int-array modifiers)]
     (handleAction [sender target] (a-fn this sender target))))
  ([name keycode a-fn] (shortcutAction name keycode a-fn [])))

;; Base components - Button, Link, Label etc.

(defn button
  "Create a Button component from constructor arguments or a configuration Map"
  [& args]
  (create-widget Button args))


(defn link
  "Usage: (button ctor_args? config_map?)

  Create a Link component from constructor arguments or a single configuration Map"
  [& args]
  (create-widget Link args))

(defn label
  "Usage: (label ctor_args? config_map?)

  Create a Label component from constructor arguments or a configuration Map"
  [& args]
  (create-widget Label args))

;; Fields

(defn text-field
  "Usage: (text-field ctor_args? config_map?)

  Create a TextField component from constructor arguments or a configuration Map."
  [& args]
  (create-widget TextField args))

(defn password-field
  "Usage: (password-field ctor_args? config_map?)

  Create a PassowrdField component from constructor arguments or a configuration Map"
  [& args]
  (create-widget PasswordField args))

(defn text-area
  "Usage: (text-area ctor_args? config_map?)

  Create a TextArea component from constructor arguments or a configuration Map."
  [& args]
  (create-widget TextArea args))

(defn rich-text-area
  "Usage: (rich-text-area ctor_args? config_map?)

  Create a RichTextArea component from constructor arguments or a configuration Map."
  [& args]
  (create-widget RichTextArea args))

(defn inline-date-field
  "Usage: (inline-date-field ctor_args? config_map?)

  Create a InineDateField component from constructor arguments or a configuration Map."
  [& args]
  (create-widget InlineDateField args))

(defn popup-date-field
  "Usage: (popup-date-field ctor_args? config_map?)

  Create a PopupDateField component from constructor arguments and/or a configuration Map."
  [& args]
  (create-widget PopupDateField args))

(defn slider
  "Usage: (slider ctor_args? config_map?)

  Create a Slider component from constructor arguments or a configuration Map."
  [& args]
  (create-widget Slider args))

(defn check-box
  "Usage: (check-box ctor_args? config_map?)

  Create a CheckBox component from constructor arguments or a configuration Map."
  [& args]
  (create-widget CheckBox args))

(defn combo-box
  "Usage: (combo-box ctor_args? config_map?)

  Create a ComboBox component from constructor arguments or a configuration Map."
  [& args]
  (create-widget ComboBox args))

(defn twin-col-select
  "Usage: (twin-col-select ctor_args? config_map?)

  Create a TwinColSelect component from constructor arguments or a configuration Map."
  [& args]
  (create-widget TwinColSelect args))

(defn native-select
  "Usage: (native-select ctor_args? config_map?)

  Create a NativeSelect component from constructor arguments or a configuration Map."
  [& args]
  (create-widget NativeSelect args))

(defn list-select
  "Usage: (list-select ctor_args? config_map?)

  Create a ListSelect component from constructor arguments or a configuration Map."
  [& args]
  (create-widget ListSelect args))

(defn option-group
  "Usage: (option-group ctor_args? config_map?)

  Create an OptionGroup component from constructor arguments or a configuration Map."
  [& args]
  (create-widget OptionGroup args))

(defn progress-bar
  "Usage: (progress-bar ctor_args? config_map?)

  Create a ProgressBar component from constructor arguments or a configuration Map."
  [& args]
  (create-widget ProgressBar args))

(defn tree
  "Usage: (tree ctor_args? config_map?)

  Create a Tree component from constructor arguments or a configuration Map."
  [& args]
  (create-widget Tree args))

;; Containers and layouts

(defn panel
  "Usage: (panel ctor_args? config_map? children?)

  Create a Panel component from constructor arguments or a configuration Map. Content can be set as either a constructor argument,
  configuration option, or a single child. If the content is set via constructor or configuration, multiple children may be given,
  and will be added as children of the panel content (if applicable)"
  [& args]
  (let [[panel children] (create-widget Panel args true)]
    (add-children panel children)))

(defn vertical-layout
  "Usage: (vertical-layout ctor_args? config_map? children?)

  Create a VerticalLayout component from constructor arguments or a configuration Map. Remaining arguments are children.
  Expansion ration and alignment parameters are placed on the children, not the layout itself"
  [& args]
  (let [[vl children] (create-widget VerticalLayout args true)]
    (add-children vl children)))

(defn horizontal-layout
  "Usage: (horizontal-layout ctor_args? config_map? children?)

  Create a HorizontalLayout component from constructor arguments or a configuration Map. Remaining arguments are children.
  Expansion ration a alignment parameters  areplaced on the children, not the layout itself"
  [& args]
  (let [[hl children] (create-widget HorizontalLayout args true)]
    (add-children hl children)))

(defn form-layout
  "Usage: (form-layout ctor_args? config_map? children?)

  Create a FormLayout component from constructor arguments or a configuration Map. Remaining arguments are children.."
  [& args]
  (let [[hl children] (create-widget FormLayout args true)]
    (add-children hl children)))

(defn grid-layout
  "Usage: (grid-layout ctor_args? config_map? children?)

  Create a GridLayout component from constructor arguments or a configuration Map. Remaining arguments are children.
  Childen may have :position and :span configuration options to specify their position and size"
  [& args]
  (let [[hl children] (create-widget GridLayout args true)]
    (add-children hl children)))

(defn tab-sheet
  "Usage: (tab-sheet ctor_args? config_map? children?)

  Create a TabSheet componentfrom constructor arguments or a configuration Map. Remaining arguments are children.."
  [& args]
  (let [[ts children] (create-widget TabSheet args true)]
    (add-children ts children)))

(defn accordion
  "Usage: (accordion ctor_args? config_map? children?)

  Create an Accordion component from constructor arguments or a configuration Map. Remaining arguments are children.."
  [& args]
  (let [[acc children] (create-widget Accordion args true)]
    (add-children acc children)))

(defn vertical-split-panel
  "Usage: (vertical-split-panel ctor_args? config_map? children?)

  Create a VerticalSplitPanel component from constructor arguments or a configuration Map. Only zero or two children may
  be specified"
  [& args]
  (let [[sl children] (create-widget VerticalSplitPanel args true)]
    (add-children sl children)))

(defn horizontal-split-panel
  "Usage: (horizontal-split-panel ctor_args? config_map? children?)

  Create a HorizontalSplitPanel component from constructor arguments or a configuration Map. Only zero or two children may
  be specified"
  [& args]
  (let [[sl children] (create-widget HorizontalSplitPanel args true)]
    (add-children sl children)))

; A MenuBar acts like a container for MenuItems

(defn menu-bar
  "Usage: (menu-bar ctor_args? config_map? menu-items?)

  Create a MenuBar. Children must be MenuItem builders."
  [& args]
  (let [[mb items] (create-widget MenuBar args true)]
    (add-children mb items)
    mb))


(defn menu-item
  "Usage: (menu-item ctor_args? config_map? menu-items?)

  Create a menu item for the contaning menu. Children cause a sub-menu to be created."
  [name & args]
  (if (not (instance? String name))
    (bad-argument "Menu name must be a String: " name))
  (parse-menu-item name args)
  )

(defn menu-separator
  "Usage: (menu-separator)

  Create a menu separator in a menu."
  []
  (->MenItemSeparator))

;; Forms

(defmacro form
  "Usage: (form [^ComponentContainer content] [^Map config] children*)

  Create a Form. This is a pseudo component that returns a ComponentConainer with an added Field Group. The FieldGroup
  is made available for binding form (children) fields, and in all event handlers attached to form components

  The content may be specified directly as the first argument, which must be a ComponentContainer, or as a :content
  configuration option. Any remaining configuration optiond are applied to the container. If neither are present the
  contnet defaults to a FormLayout"
  [& args]
  `(with-bindings {#'*current-field-group* (FieldGroup. (PropertysetItem.))}
     (let [[l# c#] (create-form-content (list ~@args))]
       (add-children l# c#)
       (set-field-group l# *current-field-group*)
       l#)))

;; Embedded items

(defn image
  "Usage: (image ctor_args? config_map?)

  Create an Image component from constructor arguments or a configuration Map."
  [& args]
  (create-widget Image args))

(defn embedded
  "Usage: (embedded ctor_args? config_map?)

  Create an Embedded component from constructor arguments or a configuration Map."
  [& args]
  (create-widget Embedded args))

;; Tables

(defn table-column
  "Usage: (table-column property_id config_map)

  Create a table column. Only valid as a child of a Table component. The first argument must be the name of the
  data binding property that the column will bind to. Other config options can be the type (:type) and default value
  (:default) of the property, plus any of the table setColumnXXX setters. These will be configured on the table as
  for other config options"
  ([propertyId config]
   (->TableColumn
     (assoc
      (merge {:type Object :defaultValue nil} config)
      :propertyId propertyId))
   )
  ([propertyId] (table-column propertyId {}))
  )

(defn table
  "Usage: (table ctor_args? config_map? table-columns?)

  Create a Table component from constructor arguments or a configuration Map. Children must be table-column specifications"
  [& args]
  (let [[table children] (create-widget Table args true)]
    (add-children table children)))

(defn tree-table
  "Usage: (tree-table ctor_args? config_map? table-columns?)

  Create a TreeTable component from constructor arguments or a configuration Map. Children must be table-column specifications"
  [& args]
  (let [[tree-table children] (create-widget TreeTable args true)]
    (add-children tree-table children)))

;; Window

(defn window
  "Usage: (window ctor_args? config_map? children?)

  Create a Window component from constructor arguments or a configuration Map. Content may be specified as for a Panel"
  [& args]
  (if-let [ui (UI/getCurrent)]
    (let [[window children] (create-widget Window args true)]
     (add-children window children)
     (.addWindow ui window)
     window)))




