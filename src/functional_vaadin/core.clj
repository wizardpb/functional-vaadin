(ns functional-vaadin.core
  "Definition of all user functions for the libray - UI definition macro and all functions to build
  individual Vaadinwidgets"
  (:use [functional-vaadin.build-support]
        [functional-vaadin.config])
  (:import (com.vaadin.ui
             Label Embedded Link MenuBar Upload Button Calendar GridLayout
             TabSheet VerticalSplitPanel HorizontalSplitPanel Slider TextField TextArea PasswordField CheckBox
             RichTextArea InlineDateField PopupDateField Table ComboBox TwinColSelect NativeSelect
             ListSelect OptionGroup Tree TreeTable Panel VerticalLayout HorizontalLayout FormLayout
             Component UI)
           (java.util Date)))

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

;; Base components - Button, Link, Label etc.

(defn button [opts]
  (configure (Button.) opts))

(defn link [opts]
  (configure (Link.) opts))

(defn label [opt-or-text]
  (if (instance? String opt-or-text)
    (Label. opt-or-text)
    (configure (Label.) opt-or-text)))

;; Forms and Fields

(defn text-field [& args]
  (condp = (count args)
    0 (TextField.)
    1 (let [[arg] args]
        (if (instance? String arg)
          (TextField. arg)
          (configure (TextField.) arg)))
    2 (let [[arg1 arg2] args]
        (if (every? #(instance? String %1) args)
          (TextField. arg1 arg2)
          (throw (IllegalArgumentException. "Both arguments must be Strings"))))
    (throw (IllegalArgumentException. "Too many arguments for TextField"))))

(defn password-field [& args]
  (condp = (count args)
    0 (PasswordField.)
    1 (let [arg (first args)]
        (if (instance? String arg)

          (PasswordField. arg) (configure (PasswordField.) arg)))
    2 (let [[arg1 arg2] args]
        (if (every? #(instance? String %1) args)
          (PasswordField. arg1 arg2)
          (throw (IllegalArgumentException. "Both arguments must be Strings"))))
    (throw (IllegalArgumentException. "Too many arguments for PasswordField"))))

(defn text-area [& args]
  (condp = (count args)
    0 (TextArea.)
    1 (let [arg (first args)]
        (if (instance? String arg)
          (TextArea. arg)
          (configure (TextArea.) arg)))
    2 (let [[arg1 arg2] args]
        (if (every? #(instance? String %1) args)
          (TextArea. arg1 arg2)
          (throw (IllegalArgumentException. "Both arguments must be Strings"))))
    (throw (IllegalArgumentException. "Too many arguments for TextArea"))))

(defn rich-text-area [& args]
  (condp = (count args)
    0 (RichTextArea.)
    1 (let [arg (first args)]
        (if (instance? String arg)
          (RichTextArea. arg)
          (configure (RichTextArea.) arg)))
    2 (let [[arg1 arg2] args]
        (if (every? #(instance? String %1) args)
          (RichTextArea. arg1 arg2)
          (throw (IllegalArgumentException. "Both arguments must be Strings"))))
    (throw (IllegalArgumentException. "Too many arguments for RichTextArea"))))

(defn inline-date-field [& args]
  (condp = (count args)
    0 (InlineDateField.)
    1 (let [arg (first args)]
        (if (instance? String arg) (InlineDateField. arg) (configure (InlineDateField.) arg)))
    2 (let [[arg1 arg2] args]
        (if (and (instance? String arg1) (instance? Date arg2))
          (InlineDateField. ^String arg1 ^Date arg2)
          (throw (IllegalArgumentException. "Arguments must be a String and a Date"))))
    (throw (IllegalArgumentException. "Too many arguments for InlineDateField"))))

(defn popup-date-field [& args]
  (condp = (count args)
    0 (PopupDateField.)
    1 (let [arg (first args)]
        (if (instance? String arg) (PopupDateField. arg) (configure (PopupDateField.) arg)))
    2 (let [[arg1 arg2] args]
        (if (and (instance? String arg1) (instance? Date arg2))
          (PopupDateField. ^String arg1 ^Date arg2)
          (throw (IllegalArgumentException. "Arguments must be a String and a Date"))))
    (throw (IllegalArgumentException. "Too many arguments for PopupDateField"))))

;; Containers and layouts

(defn panel [opts & children]
  (add-children (configure (Panel.) opts) children))

(defn vertical-layout [opts & children]
  (add-children (configure (VerticalLayout.) opts) children))

(defn horizontal-layout [opts & children]
  (add-children (configure (HorizontalLayout.) opts) children))

(defn form-layout [opts & children]
  (add-children (configure (FormLayout.) opts) children))

;; TODO - Grid and Form Layout, Split Layouts

