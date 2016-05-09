(ns functional-vaadin.rx.operators
  "Useful operators to transform streams from Vaadin component observables"
  (:require [rx.lang.clojure.core :as rx])
  (:import (rx Observable)
           (com.vaadin.data.fieldgroup FieldGroup)
           (java.util Map)))

; TODO - An observer on a future

(defn consume-for
  "Usage: (consume-for component fn xs)
  Subscribes to an Observable xs, calling the function fn with the given component for every event received
  from xs. Unsubscribes on error and complete"
  [comp c-fn xs]
  (let [c comp]
    (rx/subscribe xs (fn [v] (c-fn c v))))
  )

(defn commit
  "Usage: (commit [xs])

  Commit a received event from a form item by calling commit on the forms field group and extracting the data as an item. The item
  is passed on to the next subscriber. Assumes it will receive either a single FieldGroup object, or a Map with
  containing a key :field-group. For the former, just the item data is passed on, in the latter case, the data is added
  to the Map under a key :item. Simply passes on the received data if there is no field group."
  [^Observable xs]
  (let [op (rx/operator*
             (fn [subscribed-o]
               (rx/subscriber subscribed-o
                 (fn [recv-o v]
                   (rx/on-next
                     recv-o
                     (condp #(instance? %1 %2) v
                       Map (if-let [fg (:field-group v)]
                             (do (.commit fg) (assoc v :item (.getItemDataSource fg))))
                       FieldGroup (do (.commit v) (.getItemDataSource v))
                       true v))))
               ))]
    (rx/lift op xs)))
