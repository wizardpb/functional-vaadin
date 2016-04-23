(ns functional-vaadin.utils
  "Generally useful utility functions"
  (:import [com.vaadin.ui AbstractComponent]))

(defn capitalize [s]
  (if (empty? s) s (str (.toUpperCase (subs s 0 1)) (subs s 1))))

(defn uncapitalize [s]
  (if (empty? s) s (str (.toLowerCase (subs s 0 1)) (subs s 1))))

(defn attach-data
  "Attach data to a Component indexed by a key. The data is stored in a Map under the key, which is in turn
  stored in the setData() attribute of the Component"
  [component key data]
  (.setData component
            (if-let [m (.getData component)]
              (assoc m key data)
              (hash-map key data)))
  )

(defn get-data [component key]
  (get (.getData component) key))
