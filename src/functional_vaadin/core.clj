(ns functional-vaadin.core
  "Definition of all user functions for the library - UI definition macro and all functions to build
  individual Vaadin widgets"
  (:require [functional-vaadin.thread-vars :refer :all])
  (:import (com.vaadin.ui Component UI)))

(defprotocol IUIData
  "A protocol to assign and lookup components by ID, and provide a shared UI data area
  addressable by hierarchical (dot-separated) symbols"
  (add-component [this component id] "Assign an ID")
  (component-at [this component-id] "Look up a component")
  (bind [this component-id location] "Bind a named component to a data location")
  (update-binding [this location fn] "Update bound data at location using the function fn")
  )

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




