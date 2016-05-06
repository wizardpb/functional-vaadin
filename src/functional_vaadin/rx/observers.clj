(ns functional-vaadin.rx.observers
  "Reactive extensions to interface with RxClojure. These are functions to generate Observables from various Vaadin events."
  (:require [functional-vaadin.event-handling :refer :all]
            [rx.lang.clojure.core :as rx])
  (:import (rx Subscriber)
           (com.vaadin.ui AbstractTextField)))

(defn buttonClicks
  "Usage: (buttonClicks btn)

  Observe button clicks for the given button. Returns the Observable. Subscribers will receive a Map
  {:source btn :event evt :field-group fg} where source is the source of the click (the btn), event is the ClickEvent
  and fg is the Fieldgroup of the form that the btn is on, or nil if there is no form"
  [btn]
  (rx/observable*
    (fn [^Subscriber sub] (onClick btn (fn [s evt fg] (.onNext sub {:source s :event evt :field-group fg}))))))

(defn valueChanges
  "Usage: (valueChanges notifier)

  Observe value changes for the given notifier. Returns the Observable. Subscribers will receive a Map
  {:source notifier :event evt :field-group fg} where source is the source of the click (the notifier), event is the ValueChangeEvent
  and fg is the Fieldgroup of the form that the notifier is on, or nil if there is no form."
  [notifier]
  (rx/observable*
    (fn [^Subscriber sub] (onValueChange notifier (fn
                                                    ([source evt] (.onNext sub {:source source :event evt}))
                                                    ([source evt fg] (.onNext sub {:source source :event evt :field-group fg})))))))

(defn mouseClicks
  "Usage: (mouseClicks notifier)

  Observe value changes for the given notifier. Returns the Observable. Subscribers will receive a Map
  {:source notifier :event evt} where source is the source of the click (the notifier) and event is the MouseClickEvent."
  [comp]
  (rx/observable*
    (fn [^Subscriber sub] (onClick comp (fn [s evt fg] (.onNext sub {:source s :event evt}))))))

(defn textChanges
  "Usage: (textChanges textField)

  Observe text changes for the given notifier. Returns the Observable. Subscribers will receive a Map
  {:source textField :event evt} where source is the source of the click (the textField) and event is the TextChangeEvent."
  [comp]
  (rx/observable*
    (fn [^Subscriber sub] (onTextChange comp (fn [s evt fg] (.onNext sub {:source s :event evt}))))))