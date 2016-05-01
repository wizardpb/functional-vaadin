(ns functional-vaadin.data-binding.container
  (:require [functional-vaadin.data-binding.item :refer :all])
  (:import [java.util Collection Map]
           [com.vaadin.data.util IndexedContainer]
           [com.vaadin.data Item Container]))

(defn- collection-type [data]
  (cond
    (instance? Map data) :map
    (instance? Collection data) :collection
    true :non-sequence))

(defn- element-type [data]
  {:pre (not-empty data)}
  (condp = (collection-type data)
    :map (collection-type (second (first data)))
    :collection (collection-type (first data))
    :non-sequence :non-sequence)
  )

(defn container-dispatch [data]
  (if (empty? data)                                         ;Special case - empty collection
    :empty
    [(collection-type data) (element-type data)])           ; Encode type of collection and element
  )

(defmulti ->Container container-dispatch)

(defmethod ->Container :default [data]
  (throw (IllegalArgumentException. (str "Cannot bind " data " to a Container component"))))

(defmethod ->Container :empty [data]
  (IndexedContainer.)
  )

(defmethod ->Container [:map :non-sequence] [data]
  (IndexedContainer. (keys data)))

(defmethod ->Container [:collection :non-sequence] [data]
  (IndexedContainer. data))

(defmethod ->Container [:map :map] [data]
  (reduce
    (fn [c [item-id item-val]]
      (initializeItem
        (.addItem c item-id)
        (fn [^Item item pid val]
          (.addContainerProperty c pid (class val), nil)
          (.setValue (.getItemProperty item pid) val))
        item-val)
      c)
    (IndexedContainer.)
    data))

(defmethod ->Container [:collection :map] [data]
  (reduce
    (fn [c [item-id item-val]]
      (initializeItem
        (.addItem c item-id)
        (fn [^Item item pid val]
          (.addContainerProperty c pid (class val), nil)
          (.setValue (.getItemProperty item pid) val))
        item-val)
      c)
    (IndexedContainer.)
    (map-indexed #(vector %1 %2) data)))

(defmethod ->Container [:map :collection] [data]
  (reduce
    (fn [c [item-id item-val]]
      (initializeItem
        (.addItem c item-id)
        (fn [^Item item pid val]
          (.addContainerProperty c pid (class val), nil)
          (.setValue (.getItemProperty item pid) val))
        (map-indexed #(vector %1 %2) item-val))
      c)
    (IndexedContainer.)
    data))

(defmethod ->Container [:collection :collection] [data]
  (reduce
    (fn [c [item-id item-val]]
      (initializeItem
        (.addItem c item-id)
        (fn [^Item item pid val]
          (.addContainerProperty c pid (class val), nil)
          (.setValue (.getItemProperty item pid) val))
        (map-indexed #(vector %1 %2) item-val))
      c)
    (IndexedContainer.)
    (map-indexed #(vector %1 %2) data)))
