(ns functional-vaadin.rx.operators
  "Useful operators to transform streams from Vaadin component observables"
  (:require [functional-vaadin.core :refer :all]
            [rx.lang.clojure.core :as rx])
  (:import (rx Observable)
           (com.vaadin.data.fieldgroup FieldGroup FieldGroup$FieldGroupInvalidValueException FieldGroup$CommitException)
           (java.util Map)
           (com.vaadin.ui UI)
           (com.vaadin.server ErrorHandlingRunnable)))

(defn consume-for
  "Usage: (consume-for component fn xs)
  Subscribes to an Observable xs, calling the function fn with the given component for every event received
  from xs. Unsubscribes on error and complete"
  [comp c-fn xs]
  (let [c comp]
    (rx/subscribe xs (fn [v] (c-fn c v))))
  )

(defn commit-error [e]
  ;(if (instance? FieldGroup$CommitException e)
  ;  (let [w
  ;        (window "Field Errors"
  ;            (apply vertical-layout
  ;              (doall (map
  ;                       (fn [[f exp]]
  ;                         (label (str (.getCaption f) ": " (.getMessage exp))))
  ;                       (.getInvalidFields e)))))]
  ;    (.center w))
  ;  (throw e))
  (throw e)
  )

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
                  (fn [recv-o v]
                    (try
                      (rx/on-next recv-o
                        (condp #(instance? %1 %2) v
                          Map (if-let [fg (:field-group v)]
                                (do (.commit fg) (assoc v :item (.getItemDataSource fg))))
                          FieldGroup (do (.commit v) (.getItemDataSource v))
                          true v))
                      (catch Exception e
                        (error-handler e)
                        (rx/on-error recv-o e)))))
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
                   (.access (UI/getCurrent)
                     (reify
                       ErrorHandlingRunnable
                       (^void run [this] (rx/on-next recv-o v))
                       (^void handleError [this ^Exception e] (rx/on-error recv-o e))))))
               ))]
    (rx/lift op xs)))
