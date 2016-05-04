(ns functional-vaadin.ui.FunctionalUI
  (:require [functional-vaadin.core :refer :all]
            [functional-vaadin.utils :refer :all])
  (:gen-class :extends com.vaadin.ui.UI
              :implements [functional_vaadin.ui.IUIDataStore]
              :main false))

(defn- component-key [id]
  (concat [:components] (parse-key id)))

(defn- binding-key [id]
  (concat [:bindings] (parse-key id)))

(defn -addComponent [this component id]
  (let [ks (component-key id)]
    (if (get-data component ks)
      (throw (IllegalArgumentException. (str "There is already a component named " id))))
    (attach-data this ks component)))

(defn -componentAt [this id]
  (get-data this (component-key id)))

(defn -bind [this component-id location-id])

(defn -updateBinding [this location-id fn])

(defn -getBindingValue [this location-id])


