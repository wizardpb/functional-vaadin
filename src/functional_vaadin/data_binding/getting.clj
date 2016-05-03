(ns functional-vaadin.data-binding.getting
  (:import (com.vaadin.data Property Item Container)))

(defmulti get-bound-value (fn [binding] (:structure binding)))

(defmethod get-bound-value :Any [binding]
  {:pre [(and (= :Property (:bind-type binding))
              (or (nil? (:binder binding))
                  (instance? Property (:binder binding))))]}
  (if-let [prop (:binder binding)]
    (.getValue ^Property prop)))

(defn- get-item-data [^Item item]
  (persistent!
    (reduce #(assoc! %1 %2 (.getValue (.getItemProperty item %2)))
            (transient {})
            (.getItemPropertyIds item))))

(defmethod get-bound-value :Map [binding]
  {:pre [(and (= :Item (:bind-type binding))
              (or (nil? (:binder binding))
                  (instance? Item (:binder binding))))]}
  (if-let [item (:binder binding)]
    (get-item-data item)))

(defmethod get-bound-value :CollectionAny [binding]
  {:pre [(and (= :Container (:bind-type binding))
              (or (nil? (:binder binding))
                  (instance? Container (:binder binding))))]}
  (if-let [^Container container (:binder binding)]
    (vec (.getItemIds container))))


(defmethod get-bound-value :CollectionMap [binding]
  {:pre [(and (= :Container (:bind-type binding))
              (or (nil? (:binder binding))
                  (instance? Container (:binder binding))))]}
  (if-let [^Container container (:binder binding)]
    (persistent!
      (reduce
        #(conj! %1 (get-item-data (.getItem container %2)))
        (transient [])
        (.getItemIds container)))))
