(ns functional-vaadin.core
  "Definition of all user functions for the libray - UI definition macro and all functions to build
  individual Vaadinwidgets"
  (:use [functional-vaadin.build-support]
        [functional-vaadin.config]
        [functional-vaadin.data-map])
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
  (set-data [this source-id data] "Set a dat avalue for a source"))

(defn- get-ui-data [ui]
  (if-let [d (.getData ui)] d {:components {} :data-map {}}))

;; Extend UI with the IUIData protocol. We store the state on the data element of the component
;(extend-type UI
;  IUIData
;  (add-component [this component id]
;    (letfn [(update-component [existing]
;              (if existing
;                (throw (IllegalArgumentException. "There is already a component with id " id))
;                component))]
;      (.setData this (update-in (get-ui-data this) :components id update-component))))
;  (component-at [this id]
;    (get-in get-ui-data :components id))
;  (set-data-source [this component source-id]
;    (letfn [(update-data-source [source-map]
;              (conj (if source-map source-map []) component))]
;      (.setData this (update-in (get-ui-data this) :data-map source-id update-data-source))))
;  (set-data [this source-id data]
;    (set-component-data (get-in (get-ui-data this) :data-map source-id) data))
;  )

(defmacro defui [^UI ui top-form]
  `(with-bindings
     {#'*current-ui* ~ui}
     (let [root ~top-form]
       (if (isa? root Component)
         (.setContent *current-ui* root)
         (throw
           (UnsupportedOperationException. "The generated UI is not a Vaadin Component"))))
     *current-ui*))

;; Base components - Button, Link, Label etc.

(defn button [arg]
  ;TODO - add constructor spec building
  (if (instance? String arg)
    (Button. arg)
    (configure (Button.) arg)))

(defn link [config]
  (configure (Link.) config))

(defn label [opt-or-text]
  (if (instance? String opt-or-text)
    (Label. ^String opt-or-text)
    (configure (Label.) opt-or-text)))

;; Forms and Fields

(defn text-field [& args]
  (condp = (count args)
    ;; TODO - convert to constructor spec creation
    0 (TextField.)
    1 (let [[arg] args]
        (if (instance? String arg)
          (TextField. ^String arg)
          (configure (TextField.) arg)))
    2 (let [[arg1 arg2] args]
        (if (every? #(instance? String %1) args)
          (TextField. ^String arg1 ^String arg2)
          (throw (IllegalArgumentException. "Both arguments must be Strings"))))
    (throw (IllegalArgumentException. "Too many arguments for TextField"))))

(defn password-field [& args]
  (condp = (count args)
    0 (PasswordField.)
    1 (let [arg (first args)]
        (if (instance? String arg)

          (PasswordField. ^String arg) (configure (PasswordField.) arg)))
    2 (let [[arg1 arg2] args]
        (if (every? #(instance? String %1) args)
          (PasswordField. ^String arg1 ^String arg2)
          (throw (IllegalArgumentException. "Both arguments must be Strings"))))
    (throw (IllegalArgumentException. "Too many arguments for PasswordField"))))

(defn text-area [& args]
  (condp = (count args)
    0 (TextArea.)
    1 (let [arg (first args)]
        (if (instance? String arg)
          (TextArea. ^String arg)
          (configure (TextArea.) arg)))
    2 (let [[arg1 arg2] args]
        (if (every? #(instance? String %1) args)
          (TextArea. ^String arg1 ^String arg2)
          (throw (IllegalArgumentException. "Both arguments must be Strings"))))
    (throw (IllegalArgumentException. "Too many arguments for TextArea"))))

(defn rich-text-area [& args]
  (condp = (count args)
    0 (RichTextArea.)
    1 (let [arg (first args)]
        (if (instance? String arg)
          (RichTextArea. ^String arg)
          (configure (RichTextArea.) arg)))
    2 (let [[arg1 arg2] args]
        (if (every? #(instance? String %1) args)
          (RichTextArea. ^String arg1 ^String arg2)
          (throw (IllegalArgumentException. "Both arguments must be Strings"))))
    (throw (IllegalArgumentException. "Too many arguments for RichTextArea"))))

(defn inline-date-field [& args]
  (condp = (count args)
    0 (InlineDateField.)
    1 (let [arg (first args)]
        (if (instance? String arg) (InlineDateField. ^String arg) (configure (InlineDateField.) arg)))
    2 (let [[arg1 arg2] args]
        (if (and (instance? String arg1) (instance? Date arg2))
          (InlineDateField. ^String arg1 ^Date arg2)
          (throw (IllegalArgumentException. "Arguments must be a String and a Date"))))
    (throw (IllegalArgumentException. "Too many arguments for InlineDateField"))))

(defn popup-date-field [& args]
  (condp = (count args)
    0 (PopupDateField.)
    1 (let [arg (first args)]
        (if (instance? String arg) (PopupDateField. ^String arg) (configure (PopupDateField.) arg)))
    2 (let [[arg1 arg2] args]
        (if (and (instance? String arg1) (instance? Date arg2))
          (PopupDateField. ^String arg1 ^Date arg2)
          (throw (IllegalArgumentException. "Arguments must be a String and a Date"))))
    (throw (IllegalArgumentException. "Too many arguments for PopupDateField"))))

;; Containers and layouts

(defn panel [config & children]
  (add-children (configure (Panel.) config) children))

(defn vertical-layout [config & children]
  (add-children (configure (VerticalLayout.) config) children))

(defn horizontal-layout [config & children]
  (add-children (configure (HorizontalLayout.) config) children))

(defn form-layout [config & children]
  (add-children (configure (FormLayout.) config) children))

(defn grid-layout [config & children]
  (add-children (configure (GridLayout.) config) children))

;; TODO - Form Layout, Split Layouts

