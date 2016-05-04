(ns functional-vaadin.data-binding.core
  (:require [functional-vaadin.data-binding.item :refer :all]
            [functional-vaadin.data-binding.container :refer :all])
  (:import
    (com.vaadin.data Property Property$Viewer Item Item$Viewer Container Container$Viewer)
    (com.vaadin.data.util ObjectProperty PropertysetItem)
    (com.vaadin.ui Component Grid Calendar)
    [java.util Collection Map Set]
    [com.vaadin.data.fieldgroup FieldGroup]))

; Set data source

(defmulti set-component-data (fn [component data] (class component)))

(defmethod set-component-data :default [component data]
  (throw (IllegalArgumentException. (str (class component) " not supported for set-component-data!!"))))

(defmethod set-component-data Property [^Property component data]
  (.setValue component data))

(defmethod set-component-data Property$Viewer [^Property$Viewer component data]
  (.setPropertyDataSource component (ObjectProperty. data (class data))))

(defmethod set-component-data Item$Viewer [^Item$Viewer component data]
  (.setItemDataSource component (->Item data)))

(defmethod set-component-data FieldGroup [component data]
  (.setItemDataSource component (->Item data)))

(defmethod set-component-data Container$Viewer [component data]
  (.setContainerDataSource component (->Container data)))

(defmethod set-component-data Grid [component data]
  (.setContainerDataSource component (->Container data)))

(defmethod set-component-data Calendar [component data]
  (.setContainerDataSource component (->Container data)))

; Get data source

(defmulti get-component-data (fn [component] (class component)))

(defmethod get-component-data :default [^Component component]
  (throw (IllegalArgumentException. (str (class component) " not supported for get-component-data!!"))))
