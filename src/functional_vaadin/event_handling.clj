(ns functional-vaadin.event-handling
  (:require [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.ui Button$ClickListener Button$ClickEvent Button Panel Image Embedded Field Label
                          Label$ValueChangeEvent AbstractTextField Table Table$HeaderClickListener Table$HeaderClickEvent
                          Table$FooterClickListener Table$FooterClickEvent Upload Upload$ChangeListener Upload$ChangeEvent
                          Upload$FailedListener Upload$FailedEvent Upload$FinishedEvent Upload$FinishedListener
                          Upload$StartedListener Upload$StartedEvent Upload$SucceededListener Upload$SucceededEvent
                          Upload$ProgressListener)
           (com.vaadin.event MouseEvents$ClickListener MouseEvents$ClickEvent FieldEvents$TextChangeListener
                             FieldEvents$TextChangeEvent FieldEvents$TextChangeNotifier)
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

(defmethod onClick Button [component act-fn]
  (.addClickListener
    component
    (reify
      Button$ClickListener
      (^void buttonClick [this ^Button$ClickEvent evt] (call-form-action act-fn evt))
      ))
  component)

(defmethod onClick Panel [component act-fn]
  (.addClickListener
    component
    (reify
      MouseEvents$ClickListener
      (^void click [this ^MouseEvents$ClickEvent evt] (call-action act-fn evt))
      ))
  component)

(defmethod onClick Image [component act-fn]
  (.addClickListener
    component
    (reify
      MouseEvents$ClickListener
      (^void click [this ^MouseEvents$ClickEvent evt] (call-action act-fn evt))
      ))
  component)

(defmethod onClick Embedded [component act-fn]
  (.addClickListener
    component
    (reify
      MouseEvents$ClickListener
      (^void click [this ^MouseEvents$ClickEvent evt] (call-action act-fn evt))
      ))
  component)

(defmulti onValueChange
  "Add a an action that occurs when a components vaue changes"
  (fn [component action] (class component)))

(defmethod onValueChange :default [comp action]
  (unsupported-op "Value change listeners on " (class comp) "not yet supported"))

(defmethod onValueChange Property$ValueChangeNotifier [component act-fn]
  (.addValueChangeListener
    component
    (reify
      Property$ValueChangeListener
      (^void valueChange [this ^Property$ValueChangeEvent evt] (call-action act-fn evt))
      ))
  component)

(defmethod onValueChange Field [component act-fn]
  (.addValueChangeListener
    component
    (reify
      Property$ValueChangeListener
      (^void valueChange [this ^Property$ValueChangeEvent evt] (call-form-action act-fn evt))
      ))
  component)

(defn onTextChange [^FieldEvents$TextChangeNotifier component act-fn]
  (.addTextChangeListener
    component
    (reify
      FieldEvents$TextChangeListener
      (^void textChange [this ^FieldEvents$TextChangeEvent evt] (call-form-action act-fn evt))
      ))
  component)

(defn onHeaderClick [table act-fn]
  (.addHeaderClickListener
    table
    (reify
      Table$HeaderClickListener
      (^void headerClick [this ^Table$HeaderClickEvent evt]
        (act-fn (.getSource evt) evt (.getPropertyId evt)))
      ))
  table)

(defn onFooterClick [table act-fn]
  (.addFooterClickListener
    table
    (reify
      Table$FooterClickListener
      (^void footerClick [this ^Table$FooterClickEvent evt]
        (act-fn (.getSource evt) evt (.getPropertyId evt)))
      ))
  table)

;; Upload events

(defn onChange [^Upload upload act-fn]
  (.addChangeListener
    upload
    (reify
      Upload$ChangeListener
      (^void filenameChanged [this ^Upload$ChangeEvent evt]
        (act-fn (.getSource evt) evt (.getFilename evt))))))

(defn onFailed [^Upload upload act-fn]
  (.addFailedListener
    upload
    (reify
      Upload$FailedListener
      (^void uploadFailed [this ^Upload$FailedEvent evt]
        (act-fn (.getSource evt) evt)))))

(defn onFinished [^Upload upload act-fn]
  (.addFinishedListener
    upload
    (reify
      Upload$FinishedListener
      (^void uploadFinished [this ^Upload$FinishedEvent evt]
        (act-fn (.getSource evt) evt)))))

(defn onStarted [^Upload upload act-fn]
  (.addStartedListener
    upload
    (reify
      Upload$StartedListener
      (^void uploadStarted [this ^Upload$StartedEvent evt]
        (act-fn (.getSource evt) evt)))))

(defn onSucceeded [^Upload upload act-fn]
  (.addSucceededListener
    upload
    (reify
      Upload$SucceededListener
      (^void uploadSucceeded [this ^Upload$SucceededEvent evt]
        (act-fn (.getSource evt) evt)))))

(defn onProgress [^Upload upload act-fn]
  (.addProgressListener
    upload
    (reify
      Upload$ProgressListener
      (^void updateProgress [this ^long readBytes ^long contentLength]
        (act-fn readBytes contentLength)))))

