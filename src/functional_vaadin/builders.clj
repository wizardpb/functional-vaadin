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

;; TODO - replace form-field with <field-builder>(<propId>, & field args)

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

;; Embedded items

(defn image [& args]
  (first (create-widget Image args false)))

(defn embedded [& args]
  (first (create-widget Embedded args false)))

;; Tables

(defn table [& args]
  )
