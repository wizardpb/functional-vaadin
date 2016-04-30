(ns functional-vaadin.data-binding.core
  (:import
    (com.vaadin.data Property Property$Viewer Item Item$Viewer Container)
    (com.vaadin.data.util ObjectProperty PropertysetItem)
    (com.vaadin.ui Component)
    [java.util Collection Map Set]))

;; TODO - immutable <-> mutable bridge

(defn- initializeItem
  "Initialize a given Item using updateFn to set a Property on the item from some element of kvdata.
  Kvdata is an iterable sequece of kv pairs (MapEnties, vectors, etc) and is iterated with reduce-kv. updateFn
  id called as (updateFn item pid val) where pid and value are the k-v values from kvdata"
  [item updateFn kvdata]
  (reduce-kv
    (fn [item pid val] (updateFn item pid val) item)
    item
    kvdata)
  )

(defn ->Item [data]
  (cond
    (or                                                     ;Use natural keys for Maps and Collections
      (instance? Collection data)
      (instance? Map data)
      ) (initializeItem (PropertysetItem.)
                        (fn [item pid val] (.addItemProperty item pid (ObjectProperty. val)))
                        data)
    (or                                                     ;Synthezize integer keys for Sets and Sequences
      (instance? Set data)
      (seq? data)
      ) (initializeItem (PropertysetItem.)
                        (fn [item pid val] (.addItemProperty item pid (ObjectProperty. val)))
                        (map-indexed #(vector %1 %2) data))
    true (throw (IllegalArgumentException. "Cannot bind an Item to " data)))
  )

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
