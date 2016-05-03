(ns functional-vaadin.ui.IUIDataStore
  (:require [functional-vaadin.data-binding.binding :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (clojure.lang Keyword)
           (com.vaadin.ui Component)))

(defprotocol
  IUIDataStore
  (addComponent  [this ^Component component ^Keyword id]
    "Add a named component, and allow it to be identified by the given id")

  (componentAt [this ^Keyword id]
    "Retreive a component at an id")

  (bind [this ^Component component ^Keyword location-id]
    "Bind a named component to a named data location using a default binding type.
    Updating the data location updates data on all bound components")


  (bindAs [this ^Keyword bind-type  ^Component component ^Keyword location-id]
    "Bind a named component to a named data location using the given binding type.
    Updating the data location updates data on all bound components")

  (updateBinding [this ^Keyword location-id update-fn]
    "Update a binding with the given function. The function reveives the target component and the current
    value of the binding. It should retrn the new value, which will update the target component(s)")

  (getBindingValue [this ^Keyword location-id]
    "Retrieve the current value of a binding, includiing any edits made by the UI")

  )

(extend-type com.vaadin.ui.UI
  IUIDataStore
  (addComponent [this ^Component component ^Keyword id]
    (let [ks (component-key id)]
      (if (get-data component ks)
        (throw (IllegalArgumentException. (str "There is already a component named " id))))
      (attach-data this ks component)))

  (componentAt [this ^Keyword id]
    (get-data this (component-key id)))

  (bind [this ^Component component ^Keyword location-id]
    (bind-component this (default-bind-type component) component location-id))

  (bindAs [this ^Keyword bind-type  ^Component component ^Keyword location-id]
    (bind-component this bind-type component location-id))

  (updateBinding [this ^Keyword location-id update-fn]
    (if-let [binding (get-data this (binding-key location-id))]
      (let [[old-value binding] (update-binding binding update-fn)]
        (if binding                                           ;Save the binding object if it was created
          (attach-data this location-id binding))
        old-value)
      (throw (IllegalArgumentException. (str "No binding with id " location-id)))))

  (getBindingValue [this ^Keyword location-id]
    (if-let [binding (get-data this (binding-key location-id))]
      (get-binding-value binding)
      (throw (IllegalArgumentException. "No binding with id " location-id))))
  )
