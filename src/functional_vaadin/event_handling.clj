(ns functional-vaadin.event-handling
  (:require [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.ui Button$ClickListener Button$ClickEvent Button Panel Image Embedded Field Label Label$ValueChangeEvent AbstractTextField Table Table$HeaderClickListener Table$HeaderClickEvent Table$FooterClickListener Table$FooterClickEvent)
           (com.vaadin.event MouseEvents$ClickListener MouseEvents$ClickEvent FieldEvents$TextChangeListener FieldEvents$TextChangeEvent FieldEvents$TextChangeNotifier)
           (com.vaadin.data Property$ValueChangeListener Property$ValueChangeEvent Property$ValueChangeNotifier)))

(defn- call-form-action [act-fn evt]
  (let [source (.getSource evt)]
    (act-fn source evt (get-field-group (form-of source)))))

(defn call-action [act-fn evt]
  (act-fn (.getSource evt) evt))

(defmulti onClick
  "Add a an action that occurs when the component is clicked"
  (fn [component action] (class component)))

(defmethod onClick :default [component action]
  (unsupported-op "Click listeners on " (class component) " not yet supported"))

(defmethod onClick Button [component action]
  (let [act-fn action]
    (.addClickListener
      component
      (reify
        Button$ClickListener
        (^void buttonClick [this ^Button$ClickEvent evt] (call-form-action act-fn evt))
        ))))

(defmethod onClick Panel [component action]
  (let [act-fn action]
    (.addClickListener
      component
      (reify
        MouseEvents$ClickListener
        (^void click [this ^MouseEvents$ClickEvent evt] (call-action act-fn evt))
        )))

  (defmethod onClick Image [component action]
    (let [act-fn action]
      (.addClickListener
        component
        (reify
          MouseEvents$ClickListener
          (^void click [this ^MouseEvents$ClickEvent evt] (call-action act-fn evt))
          )))))

(defmethod onClick Embedded [component action]
  (let [act-fn action]
    (.addClickListener
      component
      (reify
        MouseEvents$ClickListener
        (^void click [this ^MouseEvents$ClickEvent evt] (call-action act-fn evt))
        ))))

(defmulti onValueChange
  "Add a an action that occurs when a components vaue changes"
  (fn [component action] (class component)))

(defmethod onValueChange :default [comp action]
  (unsupported-op "Value change listeners on " (class comp) "not yet supported"))

(defmethod onValueChange Property$ValueChangeNotifier [obj action]
  (let [act-fn action]
    (.addValueChangeListener
      obj
      (reify
        Property$ValueChangeListener
        (^void valueChange [this ^Property$ValueChangeEvent evt] (call-action act-fn evt))
        ))))

(defmethod onValueChange Field [component action]
  (let [act-fn action]
    (.addValueChangeListener
      component
      (reify
        Property$ValueChangeListener
        (^void valueChange [this ^Property$ValueChangeEvent evt] (call-form-action act-fn evt))
        ))))

(defn onTextChange [^FieldEvents$TextChangeNotifier component action]
  (let [act-fn action]
    (.addTextChangeListener
      component
      (reify
        FieldEvents$TextChangeListener
        (^void textChange [this ^FieldEvents$TextChangeEvent evt] (call-form-action act-fn evt))
        ))))

(defn onHeaderClick [table action]
  (let [act-fn action]
    (.addHeaderClickListener
      table
      (reify
        Table$HeaderClickListener
        (^void headerClick [this ^Table$HeaderClickEvent evt]
          (act-fn (.getSource evt) evt (.getPropertyId evt)))
        ))))

(defn onFooterClick [table action]
  (let [act-fn action]
    (.addFooterClickListener
      table
      (reify
        Table$FooterClickListener
        (^void footerClick [this ^Table$FooterClickEvent evt]
          (act-fn (.getSource evt) evt (.getPropertyId evt)))
        ))))