(ns functional-vaadin.data-binding.getting
  (:import (com.vaadin.data Property Item Container)))

(defmulti get-bound-value (fn [binding] (:structure binding)))

(defmethod get-bound-value :Any [binding]
  {:pre (= :Property (:bind-type binding))}
  (.getValue ^Property (:binder binding)))

(defn- get-item-data [^Item item]
  (persistent!
    (reduce #(assoc! %1 %2 (.getValue (.getItemProperty item %2)))
            (transient {})
            (.getItemPropertyIds item))))

(defmethod get-bound-value :Map [binding]
  ;{:pre (and (= :Item (:bind-type binding)) (instance? Item (:binder binding)))}
  (get-item-data (:binder binding)))

(defmethod get-bound-value :CollectionAny [binding]
  ;{:pre (and (= :Container (:bind-type binding)) (instance? Container (:binder binding)))}
  (vector (.getItemIds (:binder binding))))

(defmethod get-bound-value :CollectionAny [binding]
  ;{:pre (and (= :Container (:bind-type binding)) (instance? Container (:binder binding)))}
  (vec (.getItemIds (:binder binding))))

(defmethod get-bound-value :CollectionMap [binding]
  ;{:pre (and (= :Container (:bind-type binding)) (instance? Container (:binder binding)))}
  (let [^Container container (:binder binding)]
    (persistent!
      (reduce
        #(conj! %1 (get-item-data (.getItem container %2)))
        (transient [])
        (.getItemIds container)))))
