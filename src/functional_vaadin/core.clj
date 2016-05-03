(ns functional-vaadin.core
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
           (com.vaadin.data.fieldgroup FieldGroup)))

;; Primary build macro

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

;; Base components - Button, Link, Label etc.

(defn button [& args]
  (first (create-widget Button args false)))

(defn link [& args]
  (first (create-widget Link args false)))

(defn label [& args]
  (first (create-widget Label args false)))

;; Forms and Fields

(defn text-field [& args]
  (create-field TextField args))

(defn password-field [& args]
  (create-field PasswordField args))

(defn text-area [& args]
  (create-field TextArea args))

(defn rich-text-area [& args]
  (create-field RichTextArea args))

(defn inline-date-field [& args]
  (create-field InlineDateField args))

(defn popup-date-field [& args]
  (create-field PopupDateField args))

(defn slider [& args]
  (create-field Slider args))

(defn check-box [& args]
  (create-field CheckBox args))

(defn combo-box [& args]
  (create-field ComboBox args))

(defn twin-col-select [& args]
  (create-field TwinColSelect args))

(defn native-select [& args]
  (create-field NativeSelect args))

(defn list-select [& args]
  (create-field ListSelect args))

(defn option-group [& args]
  (create-field OptionGroup args))

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

(defn tab-sheet [& args]
  (let [[ts children] (create-widget TabSheet args true)]
    (add-children ts children)))

(defn vertical-split-panel [& args]
  (let [[sl children] (create-widget VerticalSplitPanel args true)]
    (add-children sl children)))

(defn horizontal-split-panel [& args]
  (let [[sl children] (create-widget HorizontalSplitPanel args true)]
    (add-children sl children)))

;; Forms


(defmacro form [& args]
  `(with-bindings {#'*current-field-group* (FieldGroup.)}
     (let [[l# c#] (create-form-layout (list ~@args))]
       (add-children l# c#)
       (attach-data l# :field-group *current-field-group*)
       l#)))

;; Embedded items

(defn image [& args]
  (first (create-widget Image args false)))

(defn embedded [& args]
  (first (create-widget Embedded args false)))

;; Tables

(def valid-column-options
      #{:propertyId :type :defaultValue :header :icon :alignment})

(defn table-column
  ; Config options:
  ;
  ; Class<T> type,
  ; Object defaultValue,
  ; String columnHeader,
  ; Resource columnIcon,
  ; Align columnAlignment
  ([^String propertyId ^Map config]
   (-> (assoc
         (merge {:type Object :defaultValue nil} config)
         :propertyId propertyId)
       (convert-column-values)
       (validate-column-options)))

  ([^String propertyId] (table-column propertyId {}))

  )

(defn table [& args]
  (let [[table children] (create-widget Table args true)]
    (add-children table children)))


