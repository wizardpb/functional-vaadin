(ns functional-vaadin.data-binding.conversion
  "Utilities for converting between Clojure data strctures and Vaadin data binding objects -
  Property, Item and Container"
  (:import (com.vaadin.data.util ObjectProperty PropertysetItem IndexedContainer)
           (java.util Map Collection)
           (com.vaadin.data Property Item Container)))

(defn ->Property
  ([data] (->Property data (if data (class data) Object)))
  ([data type] (ObjectProperty. data type)))

(defn <-Property [^Property property]
  {:pre [(instance? Property property)]}
  (.getValue property))

(defn ->Item [data]
  {:pre [instance? Map data]}
  (reduce (fn [item [k v]] (.addItemProperty item k (->Property v)) item)
          (PropertysetItem.)
          data))

(defn <-Item [^Item item]
  {:pre [(instance? Item item)]}
  (persistent!
    (reduce (fn [map pid] (assoc! map pid (.getValue (.getItemProperty item pid))))
            (transient {})
            (.getItemPropertyIds item)))
  )

(defn- add-data-map-item
  "Add a Container Item represented by a Map. The id is the data index, and the Map is the data-value
  Container property ids are the union of all Maps added"
  [container item-id map-item]
  (.addItem container item-id)
  (doseq [[k v] map-item]
    (if (not ((set (.getContainerPropertyIds container)) k))
      (.addContainerProperty container k (class v) nil))
    (.setValue (.getContainerProperty container item-id k) v))
  container)

(defn- add-data-value-item
  "Add a Container Item with an item id that is the data value"
  [container data-id data-value]
  (.addItem container data-value)
  container)

(defn- create-container [data add-fn]
  (let [data-vec (vec data)
        ^IndexedContainer container (IndexedContainer.)]
    (doseq [data-index (range 0 (count data-vec))]
      (add-fn container data-index (nth data data-index)))
    container))

(defmulti ->Container "Create a Container from some data"
          (fn [data]
            {:pre [(instance? Collection data)]}
            (let [vec-data (vec data)]
              (cond
               (empty? data) :CollectionAny
               (instance? Map (first vec-data)) :CollectionMap
               (not (instance? Collection (first vec-data))) :CollectionAny
               true :Unknown))
            ))

(defmethod ->Container :CollectionAny [data]
  (create-container data add-data-value-item))

(defmethod ->Container :CollectionMap [data]
  (create-container data add-data-map-item))

(defmethod ->Container :Unknown [data]
  (throw (IllegalArgumentException. (str "Cannot create a Container from " data))))

(defn <-Container [^Container container]
  {:pre [(instance? Container container)]}
  (let [item-ids (.getItemIds container)
        prop-ids (.getContainerPropertyIds container)]
    (if (or (empty? item-ids) (empty? prop-ids))
     (vec item-ids)
     (reduce (fn [data item-id]
               (conj data
                     (persistent!
                  (reduce #(assoc! %1 %2 (.getValue (.getContainerProperty container item-id %2)))
                          (transient {})
                          prop-ids))))
             []
             item-ids)
     )))
