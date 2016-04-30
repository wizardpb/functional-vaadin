(ns functional-vaadin.data-binding.core
  (:import
    (com.vaadin.data Property Property$Viewer Item Item$Viewer Container)
    (com.vaadin.data.util ObjectProperty PropertysetItem)
    (com.vaadin.ui Component)
    [java.util Collection Map Set]))

;; TODO - immutable <-> mutable bridge


(defmulti set-component-data (fn [component data] (class component)))

(defmethod set-component-data :default [component data]
  (throw (IllegalArgumentException. (str (class component) " not supported for set-component-data!!"))))

(defmethod set-component-data Property [^Property component data]
  (.setValue component data))

(defmethod set-component-data Property$Viewer [^Property$Viewer component data]
  (.setPropertyDataSource component (ObjectProperty. data (class data))))

(defmethod set-component-data Item$Viewer [^Item$Viewer component data]
  (.setItemDataSource component (->Item data)))

(defmethod set-component-data Item [^Container component data]
  )

(defmulti get-component-data (fn [component] (class component)))

(defmethod get-component-data :default [^Component component]
  (throw (IllegalArgumentException. (str (class component) " not supported for get-component-data!!"))))
