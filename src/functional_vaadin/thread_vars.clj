(ns functional-vaadin.thread-vars
  (:import [com.vaadin.data.fieldgroup FieldGroup]))

; TODO - replace with (UI/getCurrent) ?
(def
  ^{:dynamic true}
  *current-ui*
  "A dynamic var that will hold the current ui during building" nil)

(def
  ^{:dynamic true :tag FieldGroup}
  *current-field-group*
  "A dynamic var that holds the field group of any form being built" nil)