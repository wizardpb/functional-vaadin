(ns examples.simple.main-ui
  (:require [clojure.reflect :refer [reflect]])
  (import [com.vaadin.ui
           VerticalLayout
           Label
           Button
           Button$ClickListener
           Notification])
  (:gen-class
    :name examples.simple.main-ui
    :extends com.vaadin.ui.UI))

(defn- create-button-click-listener
  [action]
  (reify Button$ClickListener
    (buttonClick
      [button evt]
      (action button evt))))

(defn- add-action
  [button action]
  (.addClickListener button (create-button-click-listener action)))

(defn- create-button
  [caption action]
  (doto (Button. caption) (add-action action)))

(defn- show-click-message
  [_ evt]
  (Notification/show (str (keys (bean evt)))))

(defn- create-main-layout
  []
  (doto (VerticalLayout.)
    (.addComponent (Label. "Hello Clojure!"))
    (.addComponent (create-button "Press me!" show-click-message))))

(defn -init
  [main-ui _]
  (doto main-ui (.setContent (create-main-layout))))
