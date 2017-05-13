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

                  Certain config items can take more convenient specification of value:

                  :margin - can be either \"true\", indicating full margins, or an array of any combination of :top, :bottom,
                            :left, :right, :vertical or :horizontal, indicating that margins should be added to the
                            respective location(s)

                  :validateWith - adds a validator to a Field. The argument is any Vaadin Validator instance. There is also
                            a Clojure type FunctionalValidator which can be used to create a Validator from an arbitrary function.

                  :id - will set the component id, and also allow lookup via (component-named). This removes the need to
                        use temporary variables when referencing already-built components.

                  :addStyleNamed - will add a style to a component. The name is a String

                  Positioning and expansion options that Vaadin requires specified on the parent can now be placed on the child -
                  these include :expansionRatio, :componentAlignment, :position and :span

  children:       are the Components child Components, which will be added as appropriate
                  "

  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [functional-vaadin.naming :as nm]
            [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.build-support :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.ui
             MenuBar
             Label Embedded Link Button
             Panel VerticalLayout HorizontalLayout FormLayout GridLayout TabSheet VerticalSplitPanel HorizontalSplitPanel
             TextField TextArea PasswordField RichTextArea InlineDateField PopupDateField Slider CheckBox
             ComboBox TwinColSelect NativeSelect ListSelect OptionGroup
             Table Tree TreeTable Accordion
             Component UI Image ProgressBar Window Upload LoginForm$LoginListener LoginForm$LoginEvent)
           (com.vaadin.data.fieldgroup FieldGroup)
           (com.vaadin.data.util PropertysetItem)
           (com.vaadin.event ShortcutAction Action$Listener ActionManager)
           (com.vaadin.data.util.converter Converter)
           (functional_vaadin.ui LoginForm)))

; TODO - grid, calendar, popupview, browser-frame, audio, video, color picker, flash, notification
; TODO - layouts: absolute, css, custom(?)
; TODO - registering custom components? Custon layout, field, component
; TODO - drag and drop

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

(defn ^{:deprecated "0.3.0"} shortcutAction
  "Deprecated. Use (->ShortcutAction) instead
  "
  ([name keycode a-fn modifiers]
   (proxy [ShortcutAction Action$Listener] [name (int keycode) (int-array modifiers)]
     (handleAction [sender target] (a-fn this sender target))))
  ([name keycode a-fn] (shortcutAction name keycode a-fn []))
  )

(deftype FunctionalConverter [to-model-fn to-presn-fn model-type presn-type]
  Converter
  (convertToModel [this value type locale] (to-model-fn value type locale))
  (convertToPresentation [this value type locale] (to-presn-fn value type locale))
  (getModelType [this] model-type)
  (getPresentationType [this] presn-type)
  )

;; Base components - Button, Link, Label etc.

(defn button
  "Usage: (button ctor_args? config_map?)

  Create a Button component from constructor arguments or a configuration Map"
  [& args]
  (create-widget Button args))


(defn link
  "Usage: (link ctor_args? config_map?)

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

(defn upload
  "Usage: (upload ctor_args? config_map?)

  Create an Upload component from constructor arguments or a configuration Map."
  [& args]
  (create-widget Upload args))

(s/def ::login-form-args
  (s/cat :config (s/? map?) :login-fn fn?))

(defn- onLogin [^LoginForm source act-fn]
  (.addLoginListener
    source
    (reify
      LoginForm$LoginListener
      (^void onLogin [this ^LoginForm$LoginEvent event]
        (act-fn (.getSource event) event (.getLoginParameter event "username") (.getLoginParameter event "password")))))
  source)

(defn login-form
  "Usage: (login-form config_map? login-fn)

  Create an LoginForm component from a login function and an optional configuration Map. The login function will be called
  with arguments \"[source event username password]\" when the loginform is submitted.

  The optional config can set the usual setters on LoginForm, and also accepts options
  to alter the creation of the login button, user name and password fields:

  option :loginButtonFunc takes a function that should return the login button component. It must be a Button
  option :usernameFieldFunc takes a function that should return the user name field. It must be a TextField
  option :passwordFieldFunc takes a function that should return the password field. It must be a TextField

  Defaults for these are as for com.vaadin.ui.LoginForm"
  [& args]
  (let [parsed-args (s/conform ::login-form-args args)]
    (if (= parsed-args ::s/invalid)
      (apply bad-argument (if (zero? (count args))
                            ["No arguments supplied to login-form"]
                            ["Bad arguments for login-form: " args]))
      (->
        (create-widget LoginForm ((fnil list {}) (:config parsed-args)))
        (onLogin (:login-fn parsed-args))))))

;(defn file-upload
;  "Usage: (file-upload config_map? filename)
;
;  A convenience function to create a file upload. \"filename\" is a server pathname where the file will be uploaded,
;  \"config_map\" is a map of configuration options:
;
;  :showProgress - show a progress bar of the upload state, true/false, default false
;  :onFailed     - a function to call on failure. Arguments are the upload and event
;  :onSucceeded  - a function to call on success. Arguments are the upload and event
;  :onChanged    - a function to call when filename is changed. Arguments are the upload, event, and the new value"
;  )

;; Containers and layouts

(defn tree
  "Usage: (tree ctor_args? config_map?)

  Create a Tree component from constructor arguments or a configuration Map."
  [& args]
  (create-widget Tree args))

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
  "Usage: (menu-item name icon_resource? [menu-fn | menu-items])

  Create a menu item for the contaning menu. \"name\" is the menu item name, \"icon\" is and optional Resource that
  defines the menu item icon. Further arguments are either a single fn defining the menu action, or further menu-items
  defining a sub-menu

  When triggered, the menu-fn is called with the selected MenuItem"

  ;[name & args]
  ;(if (not (instance? String name))
  ;  (bad-argument "Menu name must be a String: " name))
  ;(parse-menu-item name args)
  [& args]
  (let [parsed-args (s/conform ::functional-vaadin.build-support/menu-item-args args)]
    (if (= parsed-args ::s/invalid)
      (bad-argument (s/explain-str ::functional-vaadin.build-support/menu-item-args args))
      (->MenuItemSpec
        (:name parsed-args)
        (:icon_resource parsed-args)
        (second (:children parsed-args)))))
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
  "Usage: (table-column property_id config_map? gen_fn?)

  Create a table column. Only valid as a child of a Table component. The first argument must be the name of the
  data binding property that the column will bind to. Other config options can be the type (:type) and default value
  (:default) of the property, plus any of the table setColumnXXX setters. These will be configured on the table as
  for other config options.

  Generated table columns can be added by adding a generation function. This will be called as

     (gen_fn table itemId columnId)

  Both the config_map and gen_fn are optional, defaults are a standard table column of type Object and a default
  value of nil
  "
  ([propertyId config gen_fn]
   (->GeneratedTableColumn propertyId config gen_fn))
  ([propertyId config_or_fn]
   (if (fn? config_or_fn)
     (->GeneratedTableColumn propertyId {} config_or_fn)
     (->TableColumn
       propertyId
       (merge {:type Object :defaultValue nil} config_or_fn)))
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

;; ActionManager

(defn action-manager
  "Usage: (action-manager ctor_args? config_map?

  Creates an ActionManager for general use"
  [& args]
  (create-widget ActionManager args))





