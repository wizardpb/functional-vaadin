(ns functional-vaadin.functional-vaadinui
    (import [com.vaadin.ui
             VerticalLayout
             Label
             TextField
             Button
             Button$ClickListener
             Notification])
    (:gen-class
     :name functional-vaadin.functional-vaadinui
     :extends com.vaadin.ui.UI))

; Sample code from 
;     http://codebrickie.com/blog/2013/02/12/using-vaadin-7-with-clojure/

(defn- create-button-click-listener
  [action]
  (reify Button$ClickListener
            (buttonClick
              [_ evt]
              (action))))

(defn- add-action
  [button action]
  (.addListener button (create-button-click-listener action)))

(defn- create-button
  [caption action]
  (doto (Button. caption) (add-action action)))

(defn- show-click-message
  []
  (Notification/show "Button clicked"))

(defn- create-main-layout
  []
  (doto (VerticalLayout.)
          (.addComponent (Label. "Hello Clojure!"))
          (.addComponent (create-button "Press me!" show-click-message))))

(defn -init
  [main-ui request]
  (doto main-ui (.setContent (create-main-layout))))
