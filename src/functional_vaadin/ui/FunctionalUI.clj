(ns functional-vaadin.ui.FunctionalUI
  (:require [functional-vaadin.core :refer :all]
            [functional-vaadin.data-binding.binding :refer :all]
            [functional-vaadin.utils :refer :all]
   )
  (:gen-class :extends com.vaadin.ui.UI
              :implements [functional_vaadin.ui.IUIDataStore]
              :main false)
  (:import (com.vaadin.ui Component)
           (clojure.lang Keyword))
   )


(defn -addComponent
  "Add a named component, and allow it to be identified by the given id"
  [this ^Component component ^Keyword id]
  (let [ks (component-key id)]
    (if (get-data component ks)
      (throw (IllegalArgumentException. (str "There is already a component named " id))))
    (attach-data this ks component)))

(defn -componentAt
  "Retreive a component at an id"
  [this ^Keyword id]
  (get-data this (component-key id)))

(defn -bind
  "Bind a named component to a named data location using a default binding type.
  Updating the data location updates data on all bound components"
  [this ^Keyword component-id ^Keyword location-id]
  (if-let [component (get-data this (component-key component-id))]
    (bind-component this (default-bind-type component) component location-id)
    (throw (IllegalArgumentException. "No component with id " component-id))))


(defn -bindAs
  "Bind a named component to a named data location using the given binding type.
  Updating the data location updates data on all bound components"
  [this ^Keyword bind-type  ^Keyword component-id ^Keyword location-id]
  (if-let [component (get-data this (component-key component-id))]
    (bind-component this bind-type component location-id)
    (throw (IllegalArgumentException. "No component with id " component-id))))

(defn -updateBinding
  "Update a binding with the given function. The function reveives the target component and the current
  value of the binding. It should retrn the new value, which will update the target component(s)"
  [this ^Keyword location-id update-fn]
  (if-let [binding (get-data this (binding-key location-id))]
    (let [[old-value binding] (update-binding binding update-fn)]
      (if binding                                           ;Save the binding object if it was created
        (attach-data this location-id binding))
      old-value)
    (throw (IllegalArgumentException. "No binding with id " location-id))))

(defn -getBindingValue
  "Retrieve the current value of a binding, includiing any edits made by the UI"
  [this ^Keyword location-id]
  (if-let [binding (get-data this (binding-key location-id))]
    (get-binding-value binding)
    (throw (IllegalArgumentException. "No binding with id " location-id))))


