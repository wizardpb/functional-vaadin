(ns functional-vaadin.ui.test-ui-def
  (:use functional-vaadin.core
        functional-vaadin.naming
        functional-vaadin.rx.operators
        functional-vaadin.rx.observers
        functional-vaadin.utils)
  (:require [rx.lang.clojure.core :as rx])
  (:import (com.vaadin.data.util ObjectProperty PropertysetItem)
           (com.vaadin.ui VerticalLayout Button$ClickEvent Button Table UI)
           (com.vaadin.data.fieldgroup FieldGroup)
           (rx Observable)
           (java.util.concurrent TimeUnit)))


(defn define-test-ui [^UI main-ui]
  (defui
    main-ui
    (panel
      "Main Panel"
      (tab-sheet
        (horizontal-layout
          {:sizeFull [] :caption "Form and Table"}
          (form {:content VerticalLayout :id :form :margin true :sizeFull []}
                (form-layout
                  (text-field "first-name" String {:nullRepresentation ""})
                  (text-field "last-name" String {:nullRepresentation ""}))
                (horizontal-layout
                  (button {:caption "Save" :id :save-button}))
                )
          (vertical-layout
            {:margin true :sizeFull []}
            (table {:caption "People" :sizeFull [] :id :table}
                   (table-column "first-name" {:header "First Name" :width 200 })
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
  (->> (buttonClicks (componentAt main-ui :save-button))    ;Observe Save button clicks
       (commit)                                             ; Commit the form of which it is a part
       (consume-for (componentAt main-ui :table)            ; Consume the form data (in :item) and set into the table
                    (fn [table data]
                      (let [{:keys [item]} data
                            row (object-array
                                  (map #(.getValue (.getItemProperty item %1))
                                       ["first-name" "last-name"]))]
                        (.addItem table row nil))
                      )))
  (let [subscription (atom nil)                             ;Create a context with some variables
        timer (Observable/interval 1 TimeUnit/SECONDS)
        progress (componentAt main-ui :progress)
        start-button (componentAt main-ui :start-button)
        stop-button (componentAt main-ui :stop-button)
        stop-fn (fn [clickInfo]
               (when @subscription                   ; When it's subscribed, unsubscribe and remove the subscription
                 (swap! subscription (fn [s] (rx/unsubscribe s) nil))
                 (.setValue progress (float 0.0))
                 (.setEnabled start-button true)
                 (.setEnabled stop-button false)
                 ))]
    (->                                                     ; Set up the Start button to subscribe to the timer
      (buttonClicks start-button)
      (rx/subscribe (fn [clickInfo]
                      (when-not @subscription               ; When it's not subscribed, subscribe and save the subscription
                        (swap! subscription
                               (fn [_] (rx/subscribe timer
                                                     (fn [t]
                                                       (.setValue progress (float (/ t 10)))
                                                       (if (> t 10) (stop-fn {}))) ;Stop when we're done
                                                     )))
                        (.setEnabled start-button false)
                        (.setEnabled stop-button true)
                        ))))
    (->                                                     ; Set up the Stop button to unsubscribe
      (buttonClicks stop-button)
      (rx/subscribe stop-fn)))
  (.setPollInterval main-ui 500)                            ; Make the ProgressBar work - we could also use PUSH mode

  )
