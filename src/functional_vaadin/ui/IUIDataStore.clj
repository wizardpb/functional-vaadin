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

  )
