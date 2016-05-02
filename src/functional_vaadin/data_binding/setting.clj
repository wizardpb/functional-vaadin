(ns functional-vaadin.data-binding.setting
  (:require [clojure.set :as set])
  (:import (com.vaadin.data.util ObjectProperty PropertysetItem IndexedContainer)
           (com.vaadin.data Property$Viewer Item$Viewer Item Container$Viewer)
           (java.util Map Collection)))

(defn- set-datasource [binding set-fn]
  (let [binder (:binder binding)]
    (doseq [component (:components binding)]
      (set-fn component binder))
    binding))

(defmulti update-binder
          ;"Update or allocate an appropriate data binding object (Property, Item or Container) for the supplied
          ;binding. If allocated, save in the binding and connect to all components bound at this location.
          ;Return the binding if it was changed, otherwise nil"
          (fn [binding data] (get binding :structure)))

(defmethod update-binder :default [binding data]
  (throw (UnsupportedOperationException. "Unknown structure type")))

(defmethod update-binder :Any [binding data]
  (if-let [binder (:binder binding)]
    (do                                                     ; Reuse existing binding and just update
      (.setValue (:binder binding) data)
      nil)
    (set-datasource
      (assoc binding :binder (ObjectProperty. data))
      #(.setPropertyDataSource %1 %2))))

(defn ->PropertysetItem [map-data]
  (reduce
    (fn [item [pid val]]
      (.addItemProperty item pid (ObjectProperty. val (class val)))
      item)
    (PropertysetItem.) map-data))

(defmethod update-binder :Map [binding data]
  (if (not (instance? Map data))
    (throw (IllegalArgumentException. (str "Cannot bind a " (.getSimpleName (class data)) " to an Item") )))
  (let [data (or data {})]
    (if-let [item (:binder binding)]
      (doseq [pid [(keys data)]]
        (.setValue (.getItemProperty item pid) (get data pid)))
     (set-datasource
       (assoc binding :binder (->PropertysetItem data))
       #(.setItemDataSource %1 %2)))))

;; Container of any type (other than Map)
(defn update-container-any
  "Update a Container where the data themselves are the item ids"
  [container data]
  (doseq [item-id (range 0 (count data))]
    (.addItem container (nth data item-id)))
  container)

(defmethod update-binder :CollectionAny [binding data]
  ;{:pre (and (= :CollectionAny (:bind-type binding))
  ;           (every? #(instance? Container$Viewer %1) (:components binding)))}
  (if (not (instance? Collection data))
    (throw (IllegalArgumentException. (str "Cannot bind a " (.getSimpleName (class data)) " to anContainer") )))

  (let [data (or data [])]
    (if-let [container (:binder binding)]
      (do
        (.removeAllItems container)
        (update-container-any container data)
        nil)
      (set-datasource
        (assoc binding :binder (update-container-any (IndexedContainer.) data))
        #(.setContainerDataSource %1 %2)))))

; Collection of Maps
(defn- is-collection-map
  "Check that data is a collection of Maps"
  [data]
  (every? #(instance? Map %1) data))

(defn update-container-items
  "Make the range of item ids in container match that of new-ids"
  [container new-ids]
  (let [old-ids (vec (.getItemIds container))
        diff (- (count new-ids) (count old-ids))]
    (if (> diff 0)
      ;; Add items
      (doseq [id (subvec new-ids (count old-ids))]
        (.addItem container id))
      ;; Remove items
      (doseq [id (subvec old-ids (count new-ids))]
        (.removeItem container id))
      )))

(defn update-container-map
  "Update a container of items. Item ids are the data indicies."
  [container data]
  (let [item-ids (range 0 (count data))]
    (update-container-items container (vec item-ids))
    (doseq [item-id item-ids]
      (let [item-map (nth data item-id)]
        (doseq [pid (keys item-map)]
          (if (not (contains? (set (.getContainerPropertyIds container)) pid))
            (.addContainerProperty container pid (class (get item-map pid)) nil))
          (.setValue (.getContainerProperty container item-id pid) (get item-map pid))))))
  container)

(defmethod update-binder :CollectionMap [binding data]
  ;{:pre (and (= :CollectionMap (:bind-type binding))
  ;           (every? #(instance? Container$Viewer %1) (:components binding)))}
  (if (not (instance? Collection data))
    (throw (IllegalArgumentException. (str "Cannot bind a " (.getSimpleName (class data)) " to anContainer") )))
  (if (not (is-collection-map data))
    (throw (IllegalArgumentException. (str "This location can only be bound to a Collection of Maps"))))

  (let [data (or data [])]
    (if-let [container (:binder binding)]
      (do
        (update-container-map container data)
        nil)
     (set-datasource
       (assoc binding :binder (update-container-map (IndexedContainer.) data))
       #(.setContainerDataSource %1 %2)))))

(defn set-bound-value [binding new-data]
  (update-binder binding new-data))