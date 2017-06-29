;;
;; Copyright 2016 Prajna Inc. All Rights Reserved.
;;
;; This work is licensed under the Eclipse Public License v1.0 - https://www.eclipse.org/legal/epl-v10.html
;; Distrubition and use must be done under the terms of this license
;;

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