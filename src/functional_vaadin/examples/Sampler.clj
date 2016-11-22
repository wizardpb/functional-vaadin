(ns functional-vaadin.examples.Sampler
  "A simple UI that presents some UI examples in a TabSheet: a form and a table. progress bar, etc..
  The table can be filed by filling in the form and clicking 'Save'"
  (:use functional-vaadin.core
        functional-vaadin.actions
        functional-vaadin.data-binding
        functional-vaadin.event-handling
        functional-vaadin.rx.observers
        functional-vaadin.rx.operators
        functional-vaadin.examples.run
        )
  (:require [rx.lang.clojure.core :as rx])
  (:gen-class :name ^{com.vaadin.annotations.Theme "valo"} functional_vaadin.examples.Sampler
              :extends com.vaadin.ui.UI
              :main false)
  (:import (com.vaadin.ui VerticalLayout Button Table UI Alignment Upload$Receiver Upload$StartedEvent Upload)
           (com.vaadin.data.fieldgroup FieldGroup)
           (com.vaadin.annotations Theme)
           (java.util.concurrent TimeUnit)
           (rx Observable)
           (java.io OutputStream ByteArrayOutputStream)
           (com.vaadin.shared.ui AlignmentInfo$Bits)))

; TODO - add To-Do tab˙

(defn- table-action-handler []
  (letfn [(select-fn [target table actions]
            (println "Selections=" (seq (.getValue table)) )
            (if (seq (.getValue table))
              (filter #(= (.getCaption %) "Delete Selected") actions)
              (filter #(= (.getCaption %) "Delete") actions)))]
    (->ActionHandler select-fn dispatch-listener
     [(->FunctionAction "Delete" (fn [a ^Table table id] (.removeItem table id)))
      (->FunctionAction "Delete Selected"
        (fn [a ^Table table id] (let [selected (.getValue table)]
                                  (if (seq selected)
                                    (doseq [id (seq selected)] (.removeItem table id ))
                                    (.removeItem table selected)))))])))

(defn- form-and-table-tab []
  (vertical-layout {:caption "Form and Table" :sizeFull []}
    (horizontal-layout {:componentAlignment Alignment/TOP_CENTER}
     (form {:content (vertical-layout {:margin true :sizeUndefined []}) :id :form :margin true}
       (form-layout {:sizeUndefined []}
         (text-field {:bindTo ["first-name" String] :nullRepresentation "" :required true})
         (text-field {:bindTo ["last-name" String] :nullRepresentation "" :required true})
         (text-field {:bindTo ["notes" String] :nullRepresentation ""})
         )
       (horizontal-layout
         (button {:caption "Save" :id :save-button}))
       )
     (vertical-layout {:margin true :sizeUndefined [] :expandRatio 1.0}
       (table {:caption "People" :id :table
               :immediate true
               :selectable  true :multiSelect true
               :actions (table-action-handler)
               }
         (table-column "first-name" {:header "First Name"})
         (table-column "last-name" {:header "Last Name"})
         (table-column "notes" {:header "Notes" :width 300})
         )))))

(defn- background-task-tab []
  (vertical-layout {:caption "Background Task" :sizeFull []}
    (horizontal-layout {:margin true :spacing true :componentAlignment Alignment/TOP_CENTER}
      (button {:caption "Start" :id :start-button})
      (button {:caption "Stop" :id :stop-button :enabled false})
      (vertical-layout {:sizeUndefined []}
        (progress-bar {:id :progress :value (float 0.0) :width "300px"})
        (label {:value "Stopped" :id :running-state})))))

(defn food-menu-tab []
  (vertical-layout {:caption "Food Menu" :margin true :spacing true :height "100%"}
    (horizontal-layout {:margin true :alignment Alignment/TOP_CENTER}
      (add-hierarchy (tree-table "Eats!"
                      (table-column "Name" {:type String :defaultValue "" :width 200})
                      (table-column "Number" {:type Long :defaultValue nil :width 100}))
       [{["Menu"]
         [{"Beverages"
           [["Coffee" 23]
            ["Tea" 42]]}
          {"Food"
           [["Bread" 13]
            ["Cake" 11]]}]}]
       ))))

(defn file-upload-tab []
  (vertical-layout {:caption "File Upload" :width "100%"}
    (vertical-layout {:sizeUndefined [] :margin true :spacing true :componentAlignment Alignment/TOP_CENTER}
     (upload {:id :file-upload :receiver (reify
                                           Upload$Receiver
                                           (^OutputStream receiveUpload [this ^String fname ^String mineType]
                                             (ByteArrayOutputStream.)))})
      ;
      ; The current implementation of upload interrupt causes the upload to restart on Chrome and Firefox.
      ; Only Safari (AFAIK) has an implementation that works. Becaus of this, I've left out the Stop function,
      ; but left the code as an example. Feel free to uncomment (here and in setup-upload-actions) and experiment

     ;(button {:caption "Stop" :id :upload-stop-button :enabled false})
     (progress-bar {:id :upload-progress :value (float 0.0) :visible false :width "100%"})
     (label {:id :upload-state :value ""}))))

(declare login-func)

(defn login-form-tab []
  (vertical-layout {:caption "Login Forms" :margin true :spacing true :height "100%"}
    (vertical-layout {:sizeUndefined [] :margin [:top] :spacing true :componentAlignment Alignment/TOP_CENTER}
      (horizontal-layout {:spacing true }
        (panel {:caption "Default"} (login-form (fn [src evt uname pwd] (login-func uname pwd))))
        (panel {:caption "Modified"} (login-form
                                       {:usernameCaption "Enter username"
                                        :passwordCaption "And your password"
                                        :loginButtonFunc (fn [] (button "Do Login"))}
                                       (fn [src evt uname pwd] (login-func uname pwd)))))
      (label {:id :login-message}))))

(defn- setup-form-actions [main-ui]
  (->> (button-clicks (componentNamed :save-button main-ui))    ; Observe Save button clicks
    (commit)                                             ; Commit the form of which it is a part
    (consume-for (componentNamed :table main-ui)            ; Consume the form data (in :item) and set into the table
      (fn [table data]
        (let [{:keys [item]} data
              row (object-array (map #(.getValue (.getItemProperty item %1)) ["first-name" "last-name" "notes"]))]
          (.addItem table row nil))
        ))))

;
; Simulate a background job for the progress indicator by using a timer to send events (increasing integers)
; at 1 second intervals. We update the progress by subscribing to these events.
;

(defn- setup-background-actions [main-ui]
  (let [subscription (atom nil)                             ; Indicate we are running by saving the timer subsciption
        timer (->>                                          ; The timer that sends events - wrap it in UI access protection
                (Observable/interval 100 TimeUnit/MILLISECONDS)
                (with-ui-access))
        progress (componentNamed :progress main-ui )        ; The progress bar component
        start-button (componentNamed :start-button main-ui) ; Start and stop button components
        stop-button (componentNamed :stop-button main-ui)
        state-label (componentNamed :running-state main-ui)
        stop-fn (fn [clickInfo]                             ; A function that stops the 'background' job
                  (when @subscription                       ; When it's subscribed, timer is running, so unsubscribe and remove the subscription
                    (swap! subscription (fn [s] (rx/unsubscribe s) nil))
                    (.setValue progress (float 0.0))        ; Reset the progress bar, and flip button state
                    (.setEnabled start-button true)
                    (.setEnabled stop-button false)
                    (.setValue state-label "Stopped")
                    ))
        tick-fn (fn [t]                                     ; Function to count the timer ticks
                  (.setValue progress (float (/ (inc t) 100)))
                  (if (> t 99) (stop-fn {})))                ;Stop when we're done
        start-fn (fn [clickInfo]
                   (when-not @subscription               ; When it's not subscribed, subscribe and save the subscription
                     (swap! subscription (fn [_] (rx/subscribe timer tick-fn)))
                     (.setEnabled start-button false)    ; Flip button state so Start is disabled and Stop enabled
                     (.setEnabled stop-button true)
                     (.setValue state-label "Running...")
                     ))
        ]
    (->                                                     ; Set up the Start button to subscribe to the timer
      (button-clicks start-button)
      (rx/subscribe start-fn))
    (->                                                     ; Set up the Stop button to stop the action
      (button-clicks stop-button)
      (rx/subscribe stop-fn))))

(defn- set-label [label & args]
  (.setValue label (apply str args)))

(defn setup-upload-actions [main-ui]
  (let [^Upload upload (componentNamed :file-upload main-ui)
        progress (componentNamed :upload-progress main-ui)
        ;stop-button (componentNamed :upload-stop-button main-ui)
        state-label (componentNamed :upload-state main-ui)]
    (onChange upload (fn [src evt fname]
                       (.setVisible progress false)
                       (set-label state-label "")))
    (onProgress upload (fn [readBytes contentLength]
                         (set-label state-label "Upload " readBytes " bytes of " contentLength)
                         (.setValue progress (float (/ readBytes contentLength)))))
    (onStarted upload (fn [c evt]
                        ;(.setEnabled stop-button true)
                        (.setVisible progress true)
                        (set-label state-label "Uploading " (.getFilename evt) ", type " (.getMIMEType evt))))
    (onSucceeded upload (fn [c evt]
                          (set-label state-label "Upload complete")
                          ;(.setEnabled stop-button false)
                          ))
    (onFailed upload (fn [c evt] (set-label state-label "Upload failed: " (.getMessage (.getReason evt)))))
    ;(onClick stop-button (fn [btn evt form]
    ;                       (set-label state-label "Interrupting...")
    ;                       (.setVisible progress false)
    ;                       (.interruptUpload upload)))
    ))

(defn -init [^UI main-ui request]
  ; Define our UI. Use :id to capture components we'll need later
  (defui main-ui
    (panel {:caption "Functional Vaadin Sampler" :sizeFull []}
      (tab-sheet
        (form-and-table-tab)
        (background-task-tab)
        (food-menu-tab)
        (file-upload-tab)
        (login-form-tab)
        )
      )
    )

  (defn login-func [uname pwd]
    (.setValue (componentNamed :login-message main-ui) (str "Logged in as \"" uname "\" with password \"" pwd "\"")))
  (setup-form-actions main-ui)
  (setup-background-actions main-ui)
  (setup-upload-actions main-ui)
  (.setPollInterval main-ui 50)                            ; Make the ProgressBar work - we could also use PUSH mode
  )
