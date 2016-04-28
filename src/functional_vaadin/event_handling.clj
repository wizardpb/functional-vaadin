(ns functional-vaadin.event-handling
  (:require [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.ui Button$ClickListener Button$ClickEvent Button Panel Image Embedded)
           (com.vaadin.event MouseEvents$ClickListener MouseEvents$ClickEvent)))

;TODO - valueChange listener

(defmulti onClick
          "Add a an action that occurs when the component is clicked"
          (fn [component action] (class component)))

(defmethod onClick :default [component action]
  (throw (UnsupportedOperationException. "Don't know howto add a clik to a " (class component))))

(defmethod onClick Button [component action]
  (let [act-fn action]
    (.addClickListener
      component
      (reify
        Button$ClickListener
        (^void buttonClick [this ^Button$ClickEvent evt] (act-fn evt))
        ))))

(defmethod onClick Panel [component action]
  (let [act-fn action]
    (.addClickListener
      component
      (reify
        MouseEvents$ClickListener
        (^void click [this ^MouseEvents$ClickEvent evt] (act-fn evt))
        ))))

(defmethod onClick Image [component action]
  (let [act-fn action]
    (.addClickListener
      component
      (reify
        MouseEvents$ClickListener
        (^void click [this ^MouseEvents$ClickEvent evt] (act-fn evt))
        ))))

(defmethod onClick Embedded [component action]
  (let [act-fn action]
    (.addClickListener
      component
      (reify
        MouseEvents$ClickListener
        (^void click [this ^MouseEvents$ClickEvent evt] (act-fn evt))
        ))))