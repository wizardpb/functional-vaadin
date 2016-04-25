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

(defn button [ & args]
  ; TODO - allow child argument to be click function
  (first (create-widget Button args false)))

(defn link [& args]
  ; TODO - allow child argument to be click function
  (first (create-widget Link args false)))

(defn label [& args]
  (first (create-widget Label args false)))

;; Forms and Fields

(defn text-field [& args]
  (first (create-widget TextField args false)))

(defn password-field [& args]
  (first (create-widget PasswordField args false)))

(defn text-area [& args]
  (first (create-widget TextArea args false)))

(defn rich-text-area [& args]
  (first (create-widget RichTextArea args false)))

(defn inline-date-field [& args]
  (first (create-widget InlineDateField args false)))

(defn popup-date-field [& args]
  (first (create-widget PopupDateField args false)))

;; Containers and layouts

(defn panel [& args]
  (let [[panel children] (create-widget Panel args true)]
    (add-children panel children)))

(defn vertical-layout [& args]
  (let [[vl children] (create-widget VerticalLayout args true)]
    (add-children vl children)))

(defn horizontal-layout [& args]
  (let [[hl children] (create-widget HorizontalLayout args true)]
    (add-children hl children)))

(defn form-layout [& args]
  (let [[hl children] (create-widget FormLayout args true)]
    (add-children hl children)))

(defn grid-layout [& args]
  (let [[hl children] (create-widget GridLayout args true)]
    (add-children hl children)))

;; TODO - Form Layout, Split Layouts

