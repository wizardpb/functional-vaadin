(ns functional-vaadin.rx.observers
  "Reactive extensions to interface with RxClojure. These are functions to generate Observables from various Vaadin events."
  (:require [functional-vaadin.event-handling :refer :all]
            [functional-vaadin.utils :refer :all]
            [rx.lang.clojure.core :as rx])
  (:import (rx Subscriber Observable)
           (com.vaadin.ui AbstractTextField)
           (com.vaadin.event Action$Listener ShortcutAction)))

(defn button-clicks
  "Observe button clicks for the given button. Returns the Observable. Subscribers will receive a Map
  {:source btn :event evt :field-group fg} where source is the source of the click (the btn), event is the ClickEvent
  and fg is the Fieldgroup of the form that the btn is on, or nil if there is no form"
  [btn]
  (rx/observable*
    (fn [^Subscriber sub]
      (onClick btn (fn [source evt fg]
                     (.onNext sub {:source source :event evt :field-group fg}))))))

(defn value-changes
  "Observe value changes for the given notifier. Returns the Observable. Subscribers will receive a Map
  {:source notifier :event evt :field-group fg} where source is the source of the click (the notifier), event is the ValueChangeEvent
  and fg is the Fieldgroup of the form that the notifier is on, or nil if there is no form."
  [notifier]
  (rx/observable*
    (fn [^Subscriber sub]
      (onValueChange notifier
        (fn
          ([source evt] (.onNext sub {:source source :event evt}))
          ([source evt fg] (.onNext sub {:source source :event evt :field-group fg})))))))

(defn mouse-clicks
  "Observe value changes for the given notifier. Returns the Observable. Subscribers will receive a Map
  {:source notifier :event evt} where source is the source of the click (the notifier) and event is the MouseClickEvent."
  [component]
  (rx/observable*
    (fn [^Subscriber sub] (onClick component (fn [s evt fg] (.onNext sub {:source s :event evt}))))))

(defn text-changes
  "Observe text changes for the given notifier. Returns the Observable. Subscribers will receive a Map
  {:source textField :event evt} where source is the source of the click (the textField) and event is the TextChangeEvent."
  [textField]
  (rx/observable*
    (fn [^Subscriber sub] (onTextChange textField (fn [s evt fg] (.onNext sub {:source s :event evt}))))))

(defn events-in
  "Observe events from a function. On subscription, act-fn is executed asynchronously in a future and passed the subscriber (s)
  and any extra args provided. Events are indicated by using (rx/on-next s) within the function. (rx/on-completed s) is sent when the
  function completes, and any exceptions thrown are reported with (rx/on-error s e). The function should check for unsuncribes, and
  act appropriately (usually terminating)."
  [act-fn & args]
  (rx/observable* (fn [^rx.Subscriber s]
                    (future
                      (try
                        (apply act-fn (concat (list s) args))
                        (when-subscribed (.onCompleted s))
                        (catch Throwable e
                          (when-subscribed (.onError s e))))))))

(defn- event-shortcut
  [{:keys [name keycode modifiers]} a-fn]
  (proxy [ShortcutAction Action$Listener] [name (int keycode) (int-array (or modifiers []))]
    (handleAction [sender target] (a-fn this sender target)))
  )

(defn with-actions [component actions]
  (rx/observable*
    (fn [^rx.Subscriber o]
      (doseq [action actions]
        (.addAction component
          (event-shortcut action (fn [a s t] (when-subscribed o (.onNext o {:action a :sender s :target t})))))))))

; TODO - other observers - table clicks, component clicks - see notes.


