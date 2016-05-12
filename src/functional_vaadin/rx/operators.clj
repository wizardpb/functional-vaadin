(ns functional-vaadin.rx.operators
  "Useful operators to transform streams from Vaadin component observables"
  (:require [functional-vaadin.core :refer :all]
            [rx.lang.clojure.core :as rx])
  (:import (rx Observable Observer)
           (com.vaadin.data.fieldgroup FieldGroup FieldGroup$FieldGroupInvalidValueException FieldGroup$CommitException)
           (java.util Map)
           (com.vaadin.ui UI Window)
           (com.vaadin.server ErrorHandlingRunnable)
           (com.vaadin.data Validator$EmptyValueException)))

(defmacro when-subscribed [o & body]
  `(when-not (rx/unsubscribed? ~o)
     ~@body))

(defn consume-for
  "Usage: (consume-for component fn xs)
  Subscribes to an Observable xs, calling the function fn with the given component for every event received
  from xs."
  [comp c-fn xs]
  (let [c comp]
    (rx/subscribe xs
      (fn [v] (c-fn c v))
      (fn [e] )
      (fn [] )))
  )


(defn- failure-message [e]
  (if (instance? Validator$EmptyValueException e)
    "Required"
    (.getMessage e)))

(defn commit-error [e]
  (if (instance? FieldGroup$CommitException e)
    (.center
      (window "Field Errors"
        (vertical-layout {:margin true :spacing true}
          (doall (map
                   (fn [[f exp]]
                     (label (str (.getCaption f) ": " (failure-message exp))))
                   (.getInvalidFields e))))))
    (throw e))
  )

(defmulti do-commit
  "Commit a passed in FieldGroup, depending on the way it was passed"
  (fn [v] (class v)))

(defmethod do-commit Map [v]
  (if-let [fg (:field-group v)]
    (do
      (.commit fg)
      (assoc v :item (.getItemDataSource fg)))              ; Add the data Item to the Map
    v))

(defmethod do-commit FieldGroup [v]
  (do
    (.commit v)
    (.getItemDataSource v)))                                ; Just return the data item

(defmethod do-commit :default [v]                           ; Pass on the value by default
  v)


(defn commit
  "Usage: (commit [error-handler? xs])

  Commit a received event from a form item by calling commit on the forms field group and extracting the data as an item. The item
  is passed on to the next subscriber. Assumes it will receive either a single FieldGroup object, or a Map with
  containing a key :field-group. For the former, just the item data is passed on, in the latter case, the data is added
  to the Map under a key :item. Simply passes on the received data if there is no field group."
  ([error-handler ^Observable xs]
   (let [op (rx/operator*
              (fn [subscribed-o]
                (rx/subscriber subscribed-o
                  (fn [^Observer recv-o v]
                    (try
                      (when-subscribed recv-o
                        (.onNext recv-o (do-commit v)))
                      (catch Exception e
                        (error-handler e)
                        (.onError recv-o e))))
                  (fn [recv-o e]
                    (when-subscribed recv-o
                      (.onError recv-o e)))
                  )
                ))]
     (rx/lift op xs)))
  ([^Observable xs] (commit commit-error xs))
  )

(defn with-ui-access
  "Forward events to subscribers protected by a UI access lock. Uses UI.access() which hands the onNext off to a Future."
  [^Observable xs]
  (let [op (rx/operator*
             (fn [subscribed-o]
               (rx/subscriber subscribed-o
                 (fn [recv-o v]                             ;on-next
                   (when-subscribed recv-o
                     (if-let [ui (UI/getCurrent)]
                      (.access ui
                        (reify
                          ErrorHandlingRunnable
                          (^void run [this] (rx/on-next recv-o v))
                          (^void handleError [this ^Exception e] (rx/on-error recv-o e))))
                      (try
                        (rx/on-next recv-o v)
                        (catch Exception e
                          (rx/on-error recv-o e)))
                      )))
                 (fn [recv-o e]
                   (when-subscribed recv-o
                     (rx/on-error recv-o e))))
               ))]
    (rx/lift op xs)))
