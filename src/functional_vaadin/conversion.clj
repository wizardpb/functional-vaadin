(ns functional-vaadin.conversion
  "Utilities for converting between Clojure data strctures and Vaadin data binding objects -
  Property, Item and Container"
  (:import (com.vaadin.data.util ObjectProperty PropertysetItem IndexedContainer)
           (java.util Map Collection)
           (com.vaadin.data Property Item Container)))

(defn ->Property
  "Create a Property from some data. The type can be supplied directly or implied from the given
  (non-nil) data"
  ([data] (->Property data (if data (class data) Object)))
  ([data type] (ObjectProperty. data type)))

(defn <-Property
  "Extract the data in a Property"
  [^Property property]
  {:pre [(instance? Property property)]}
  (.getValue property))

(defn ->Item
  "Convert a Map object to a com.vaadin,data.Item"
  [data]
  {:pre [instance? Map data]}
  (reduce (fn [item [k v]] (.addItemProperty item k (->Property v)) item)
          (PropertysetItem.)
          data))

(defn <-Item
  "Extract the data in an Item to a Clojure hash-map" [^Item item]
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

(defmulti ->Container
          "Create a Container from some data. The data structure determines how the data is added. Collections of Maps
          add Items to the Container (as would be constructed by ->Item), and use the collection index as the Item id.
           Collections of other objects use those objects as Item ids, but do not add Properties to those items. This is
           useful for setting the data for selection components (derived from com.vaadin.ui.AbstractSelect)"
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

(defn <-Container
  "Extract data from a Container in such a way that (<-Container (->Container data)) produces the original data content
  and structure"
  [^Container container]
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
