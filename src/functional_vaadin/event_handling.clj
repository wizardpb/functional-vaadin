(ns functional-vaadin.event-handling
  (:use functional-vaadin.utils)
  (:import (com.vaadin.ui Button$ClickListener Button$ClickEvent)))

(deftype ButtonClickDelegator [handler]
  Button$ClickListener
  (^void buttonClick [this ^Button$ClickEvent evt] (handler evt)))

