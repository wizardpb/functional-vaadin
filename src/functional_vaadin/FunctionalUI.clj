(ns functional-vaadin.FunctionalUI
  (:require [functional-vaadin.core :refer :all]
            [functional-vaadin.utils :refer :all])
  (:gen-class :extends com.vaadin.ui.UI
              :implements IUIData
              :main false))
(defn- component-key [id]
  (concat [:components] (parse-key id)))

(defn- location-key)

(defn -add-component [this component id]
  (let [ks (component-key id)]
    (if (get-data component ks)
      (throw (IllegalArgumentException. (str "There is already a component named " id))))
    (attach-data this ks component)))

(defn -component-at [this id]
  (get-data this (component-key id)))

(defn -bind [this component-id location])

(defn -update-binding [this location data])


