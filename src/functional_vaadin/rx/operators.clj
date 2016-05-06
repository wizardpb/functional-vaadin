(ns functional-vaadin.rx.operators
  "Useful operators to transform streams from Vaadin component observables"
  (:require [rx.lang.clojure.core :as rx])
  (:import (rx Observable)
           (com.vaadin.data.fieldgroup FieldGroup)))

(defn commit
  "Commit a received event from a form item by committing the field group and extracting the data as an item"
  [^Observable xs]
  (let [op (rx/operator* (fn [o]
                           (rx/subscriber o (fn [o v]
                                              (let [{:keys [field-group]} v]
                                                (if field-group (.commit field-group))
                                                (rx/on-next o (assoc v :item (.getItemDataSource field-group))))))
                           ))]
    (rx/lift op xs)))
