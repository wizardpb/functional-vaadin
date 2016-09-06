(ns functional-vaadin.data-binding
  "Utilities for converting between Clojure data structures and Vaadin data binding objects -
  Property, Item and Container"
  (:require [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.data.util ObjectProperty PropertysetItem IndexedContainer HierarchicalContainer)
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
        :else :Unknown))
    ))

(defmethod ->Container :CollectionAny [data]
  (create-container data add-data-value-item))

(defmethod ->Container :CollectionMap [data]
  (create-container data add-data-map-item))

(defmethod ->Container :Unknown [data]
  (bad-argument "Cannot create a Container from " data))

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

(defn add-item-as-id [container last-id data]
  (.addItem container data)
  data)

(defn add-item-gen-id [container last-id data]
  (let [id (inc last-id)
        row (if (collection? data) data [data])
        props (vec (.getContainerPropertyIds container))]
    (.addItem container id)
    (doseq [i (range 0 (count props))]
      (when (< i (count row))
        (.setValue (.getContainerProperty container id (props i)) (row i)))
      )
    id))

(defmulti add-node (fn [container allow-children parent-id last-id child add-fn] (if (instance? Map child) :tree :leaf)))

(declare add-tree)

(defmethod add-node :leaf [container allow-children parent-id last-id child add-fn]
  (let [child-id (add-fn container last-id child)]
    (.setChildrenAllowed container child-id allow-children)
    (when parent-id
      (.setParent container child-id parent-id))
    child-id))

(defmethod add-node :tree [container allow-children parent-id last-id child add-fn]
  (let [tree-parent (first (keys child))]
    (add-tree container allow-children parent-id last-id tree-parent (get child tree-parent) add-fn)))

(defn add-tree [container allow-children parent-id last-id tree-parent children add-fn]
  {:pre [(not (instance? Map tree-parent)), (collection? children)]}
  (let [tree-parent-id (add-node container true parent-id last-id tree-parent add-fn)]
    (loop [remaining-children children
           last-id tree-parent-id]
      (if (empty? remaining-children)
        last-id
        (recur
          (rest remaining-children)
          (add-node container allow-children tree-parent-id last-id (first remaining-children) add-fn))))))

(defn- choose-id-gen
  "Decide on the add method depending on the form of the data spec. Collection nodes use :gen-id, otherwise it's :add-id"
  [hdef]
  (if (or
        (empty? hdef)
        (collection? (first hdef))
        (and (instance? Map (first hdef)) (collection? (first (keys (first hdef))))))
    :gen-id
    :as-id))

(defn add-hierarchy
  "Add data to a Container$Hierarchical. The data are (recursively) a Sequence of Maps, each Map defining a parent (the key)
  and the children (the value, another Sequence of Maps).

  add-as determines how items are added. :gen-id causes item ids to be generated, based on add index, :as-id uses the parent itself
  as the id. The former is generally used when adding to a container with item properties (such as a TreeTable), the later when the
  container only consideres the item ids themselves (as in a Tree). In the :gen-id case, keys and values are assumed to be Collections,
  and are mapped by index to the Container properties."
  ([container hdef allow-children add-as]
   (when-not (#{:gen-id :as-id} add-as)
     (bad-argument "Incorrect add specification: " add-as))
   (let [add-fn (if (= add-as :gen-id) add-item-gen-id add-item-as-id)]
     (loop
      [remaining-nodes hdef
       last-id -1]
      (if (empty? remaining-nodes)
        container
        (recur
          (rest remaining-nodes)
          (add-node container allow-children nil last-id (first remaining-nodes) add-fn))))))
  ([container hdef] (add-hierarchy container hdef false (choose-id-gen hdef)))
  )

(defn ->Hierarchical [hdef]
  (add-hierarchy (HierarchicalContainer.) hdef))
