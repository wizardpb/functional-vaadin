;;
;; Copyright 2016 Prajna Inc. All Rights Reserved.
;;
;; This work is licensed under the Eclipse Public License v1.0 - https://www.eclipse.org/legal/epl-v10.html
;; Distrubition and use must be done under the terms of this license
;;
(ns functional-vaadin.rx.operators
  "Useful operators to transform streams from Vaadin component observables"
  (:require [functional-vaadin.core :refer :all]
            [functional-vaadin.utils :refer :all]
            [rx.lang.clojure.core :as rx])
  (:import (rx Observable Observer)
           (com.vaadin.data.fieldgroup FieldGroup FieldGroup$FieldGroupInvalidValueException FieldGroup$CommitException)
           (java.util Map)
           (com.vaadin.ui UI Window)
           (com.vaadin.server ErrorHandlingRunnable)
           (com.vaadin.data Validator$EmptyValueException)))

(defn consume-for
  "Usage: (consume-for component fn xs)

  Subscribes to an Observable xs, calling the function fn with the given component for every event received
  from xs."
  [comp c-fn xs]
  (let [c comp]
    (rx/subscribe xs
      (fn [v] (c-fn c v))
      (fn [e])
      (fn [])))
  )

(defn- failure-message [e]
  (if (instance? Validator$EmptyValueException e)
    "Required"
    (.getMessage e)))

(defn- default-commit-error-handler [e]
  (if (instance? FieldGroup$CommitException e)
    (.center
      (window "Field Errors"
        (apply vertical-layout
          (concat (list {:margin true :spacing true})
            (map
              (fn [[f exp]]
                (label (str (.getCaption f) ": " (failure-message exp))))
              (.getInvalidFields e))))))
    (throw e))
  )

(defn- do-commit [v]
  (condp instance? v
    Map (if-let [fg (:field-group v)]
          (do
            (.commit fg)
            (assoc v :item (.getItemDataSource fg)))        ; Add the data Item to the Map
          )
    FieldGroup (do
                 (.commit v)
                 (.getItemDataSource v))
    ; default - pass the item through
    v))

(defn commit
  "Usage: (commit error-handler? xs)

  Commit a received event from a form item by calling commit on the forms field group and extracting the data as an item. The item
  is passed on to the next subscriber. Assumes it will receive either a single FieldGroup object, or a Map with
  containing a key :field-group. For the former, just the item data is passed on, in the latter case, the data is added
  to the Map under a key :item. Simply passes on the received data if there is no field group."
  ([commit-error-handler ^Observable xs]
   (let [op (rx/operator*
              (fn [subscribed-o]
                (rx/subscriber subscribed-o
                  (fn [^Observer recv-o v]
                    (when-subscribed recv-o
                      (try
                        (.onNext recv-o (do-commit v))
                        (catch FieldGroup$CommitException e
                          (commit-error-handler e))
                        (catch Throwable t
                          (.onError recv-o t)))))
                  (fn [recv-o e]
                    (when-subscribed recv-o
                      (.onError recv-o e)))
                  )
                ))]
     (rx/lift op xs)))
  ([^Observable xs] (commit default-commit-error-handler xs))
  )

(defn with-ui-access
  "Usage: (with-ui-access xs)

  Forward events to subscribers protected by a UI access lock. Uses UI.access() which hands the onNext off to a Future."
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
