(ns functional-vaadin.builders
  (:require [functional-vaadin.build-support :refer :all]
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


;; TODO - data binding,

;; Base components - Button, Link, Label etc.

(defn button [& args]
  (first (create-widget Button args false)))

(defn link [& args]
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

(defn slider [& args]
  (first (create-widget Slider args false)))

(defn check-box [& args]
  (first (create-widget CheckBox args false)))

(defn combo-box [& args]
  (first (create-widget ComboBox args false)))

(defn twin-col-select [& args]
  (first (create-widget TwinColSelect args false)))

(defn native-select [& args]
  (first (create-widget NativeSelect args false)))

(defn list-select [& args]
  (first (create-widget ListSelect args false)))

(defn option-group [& args]
  (first (create-widget OptionGroup args false)))

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

(def ^{:dynamic true :tag FieldGroup} *current-field-group* nil)

(defn create-form-layout [arg-list]
  (let [[conf# & rest#] arg-list]
    (if (and (instance? Map conf#) (:content conf#))
      (create-widget (:content conf#) (concat (list (dissoc conf# :content)) rest#) true)
      (create-widget FormLayout arg-list true))))

(defmacro form [& args]
  `(with-bindings {#'*current-field-group* (FieldGroup.)}
     (let [[l# c#] (create-form-layout (list ~@args))]
       (add-children l# c#)
       (attach-data l# :field-group *current-field-group*)
       l#
       )))

(defn form-field [propertId klass & config]
  (when (not (isa? klass Field))
    (throw (IllegalArgumentException. (str "Form field can only be created from instances of " Field))))
  (when (nil? *current-field-group*)
    (throw (UnsupportedOperationException. "Form fields cannot be created outside of forms")))
  (let [[f c] (create-widget klass (list (or (first config) {:caption (humanize propertId)})) false)]
    (.bind *current-field-group* f propertId)
    f))

;; Embedded items

(defn image [& args]
  (first (create-widget Image args false)))

(defn embedded [& args]
  (first (create-widget Embedded args false)))
