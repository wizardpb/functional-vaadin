(ns functional-vaadin.event-handling
  (:require [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.ui Button$ClickListener Button$ClickEvent Button Panel Image Embedded Field Label Label$ValueChangeEvent)
           (com.vaadin.event MouseEvents$ClickListener MouseEvents$ClickEvent)
           (com.vaadin.data Property$ValueChangeListener Property$ValueChangeEvent)))

(defmulti onClick
          "Add a an action that occurs when the component is clicked"
          (fn [component action] (class component)))

(defmethod onClick :default [component action]
  (throw (UnsupportedOperationException. "Don't know howto add a clik to a " (class component))))

(defmethod onClick Button [component action]
  (let [act-fn action]
    (.addClickListener
      component
      (let [fg *current-field-group*]
        (reify
         Button$ClickListener
         (^void buttonClick [this ^Button$ClickEvent evt] (act-fn evt fg))
         )))))

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

(defmulti onValueChange
          "Add a an action that occurs when a components vaue changes"
          (fn [component action] (class component)))

(defmethod onValueChange Field [component action]
  (let [act-fn action]
    (.addValueChangeListener
     component
     (reify
       Property$ValueChangeListener
       (^void valueChange [this ^Property$ValueChangeEvent evt] (act-fn evt))
       ))))

(defmethod onValueChange Label [component action]
  (let [act-fn action]
    (.addValueChangeListener
      component
      (reify
        Property$ValueChangeListener
        (^void valueChange [this ^Property$ValueChangeEvent evt] (act-fn evt))
        ))))