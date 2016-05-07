(ns functional-vaadin.examples.Sampler
  "A simple UI that presents some UI examples in a TabSheet: a form and a table. progress bar, etc..
  The table can be filed by filling in the form and clicking 'Save'"
  (:use functional-vaadin.core
        functional-vaadin.naming
        functional-vaadin.rx.observers
        functional-vaadin.rx.operators
        functional-vaadin.utils)
  (:require [rx.lang.clojure.core :as rx])
  (:gen-class :name ^{com.vaadin.annotations.Theme "valo"} functional_vaadin.examples.Sampler
              :extends com.vaadin.ui.UI
              :main false)
  (:import (com.vaadin.ui VerticalLayout Button Table UI)
           (com.vaadin.data.fieldgroup FieldGroup)
           (com.vaadin.annotations Theme)
           (java.util.concurrent TimeUnit)
           (rx Observable)))

(defn -init [^UI main-ui request]
  (defui                                                    ; Define our UI. Use :id to capture components we'll need later
    main-ui
    (panel
      "Main Panel"
      (tab-sheet
        (horizontal-layout
          {:sizeFull [] :caption "Form and Table"}
          (form {:content VerticalLayout :id :form :margin true :sizeFull []}
                (form-layout
                  (text-field "first-name" String)
                  (text-field "last-name" String))
                (horizontal-layout
                  (button {:caption "Save" :id :save-button}))
                )
          (vertical-layout
            {:margin true :sizeFull []}
            (table {:caption "People" :sizeFull [] :id :table}
                   (table-column "first-name" {:header "First Name" :width 200})
                   (table-column "last-name" {:header "Last Name"})
                   )
            )
          )
        (vertical-layout
          {:caption "Background Task"}
          (horizontal-layout
            {:margin true :spacing true}
            (button {:caption "Start" :id :start-button})
            (button {:caption "Stop" :id :stop-button :enabled false})
            (progress-bar {:id :progress :value (float 0.0) :width "300px"}))

          )
        )
      )
    )
  (->> (buttonClicks (componentAt main-ui :save-button))    ; Observe Save button clicks
       (commit)                                             ; Commit the form of which it is a part
       (consume-for (componentAt main-ui :table)            ; Consume the form data (in :item) and set into the table
                    (fn [table data]
                      (let [{:keys [item]} data
                            row (object-array
                                  (map #(.getValue (.getItemProperty item %1))
                                       ["first-name" "last-name"]))]
                        (.addItem table row nil))
                      )))
  ;
  ; Simulate a background job for the progress indicator by using a timer to send events (increasing integers)
  ; at 1 second intervals. We update the progress by subscribing to these events.
  ;
  (let [subscription (atom nil)                             ; Indicate we are running by saving the timer subsciption
        timer (Observable/interval 1 TimeUnit/SECONDS)      ; The timer that sends events
        progress (componentAt main-ui :progress)            ; The progress bar component
        start-button (componentAt main-ui :start-button)    ; Start and stop button components
        stop-button (componentAt main-ui :stop-button)
        stop-fn (fn [clickInfo]                             ; A function that stops the 'background' job
                  (when @subscription                       ; When it's subscribed, timer is running, so unsubscribe and remove the subscription
                    (swap! subscription (fn [s] (rx/unsubscribe s) nil))
                    (.setValue progress (float 0.0))        ; Reset the progress bar, and flip button state
                    (.setEnabled start-button true)
                    (.setEnabled stop-button false)
                    ))]
    (->                                                     ; Set up the Start button to subscribe to the timer
      (buttonClicks start-button)
      (rx/subscribe (fn [clickInfo]
                      (when-not @subscription               ; When it's not subscribed, subscribe and save the subscription
                        (swap! subscription                 ; Also indicate when we are done by using stop-fn
                               (fn [_] (rx/subscribe timer
                                                     (fn [t]
                                                       (.setValue progress (float (/ t 10)))
                                                       (if (> t 10) (stop-fn {}))) ;Stop when we're done
                                                     )))
                        (.setEnabled start-button false)    ; Flip button state so Start is disabled and Stop enabled
                        (.setEnabled stop-button true)
                        ))))
    (->                                                     ; Set up the Stop button to stop the action
      (buttonClicks stop-button)
      (rx/subscribe stop-fn)))
  (.setPollInterval main-ui 500)                            ; Make the ProgressBar work - we could also use PUSH mode
  )
