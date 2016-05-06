(ns functional-vaadin.rx.observers
  "Reactive extensions to interface with RxClojure. These are functions to generate Observables from various Vaadin events."
  (:require [functional-vaadin.event-handling :refer :all]
            [rx.lang.clojure.core :as rx])
  (:import (rx Subscriber)
           (com.vaadin.ui AbstractTextField)))

(defn buttonClicks [btn]
  (rx/observable*
    (fn [^Subscriber sub] (onClick btn (fn [s evt fg] (.onNext sub {:source s :event evt :field-group fg}))))))

(defn valueChanges [notifier]
  (rx/observable*
    (fn [^Subscriber sub] (onValueChange notifier (fn
                                                    ([source evt] (.onNext sub {:source source :event evt}))
                                                    ([source evt fg] (.onNext sub {:source source :event evt :field-group fg})))))))

(defn mouseClicks [comp]
  (rx/observable*
    (fn [^Subscriber sub] (onClick comp (fn [s evt fg] (.onNext sub {:source s :event evt}))))))

(defn textChanges [comp]
  (rx/observable*
    (fn [^Subscriber sub] (onTextChange comp (fn [s evt fg] (.onNext sub {:source s :event evt}))))))