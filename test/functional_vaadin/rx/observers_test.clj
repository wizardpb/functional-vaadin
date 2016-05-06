(ns functional-vaadin.rx.observers-test
  (:require [clojure.test :refer :all]
            [rx.lang.clojure.core :as rx]
            [functional-vaadin.core :refer :all]
            [functional-vaadin.rx.observers :refer :all]
            [functional-vaadin.rx.operators :refer :all]
            [functional-vaadin.utils :refer :all]
            )
  (:import (java.util Map)
           (com.vaadin.ui Button Button$ClickEvent FormLayout TextField Label$ValueChangeEvent Field$ValueChangeEvent)))

(deftest rx-button
  (testing "Creation/subscribe"
    (let [^Button b (button)
          fired (atom nil)]
      (-> (buttonClicks b)
        (rx/subscribe (fn [v] (swap! fired (fn [_] v)))))
      (.click b)
      (is (instance? Map @fired))
      (is (identical? (:source @fired) b))
      (is (instance? Button$ClickEvent (:event @fired)))
      (is (nil? (:field-group @fired)))
      )
    (let [^FormLayout form (form (button))
          ^Button b (.getComponent form 0)
          fired (atom nil)]
      (-> (buttonClicks b)
          (rx/subscribe (fn [v] (swap! fired (fn [_] v)))))
      (.click b)
      (is (instance? Map @fired))
      (is (identical? (:source @fired) b))
      (is (instance? Button$ClickEvent (:event @fired)))
      (is (identical? (get-field-group form) (:field-group @fired)))
      )
    ))

(deftest rx-value-change
  (testing "Creation/subscribe"
    (let [^TextField field (text-field)
          fired (atom nil)]
      (-> (valueChanges field)
          (rx/subscribe (fn [v] (swap! fired (fn [_] v)))))
      (.setValue field "New Text")
      (is (instance? Map @fired))
      (is (identical? (:source @fired) field))
      (is (instance? Field$ValueChangeEvent (:event @fired)))
      (is (nil? (:field-group @fired)))
      )
    (let [form (form (text-field "prop"))
          ^TextField field (.getComponent form 0)
          fired (atom nil)]
      (-> (valueChanges field)
          (rx/subscribe (fn [v] (swap! fired (fn [_] v)))))
      (.setValue field "New Text")
      (is (instance? Map @fired))
      (is (identical? (:source @fired) field))
      (is (instance? Field$ValueChangeEvent (:event @fired)))
      (is (identical? (get-field-group form) (:field-group @fired)))
      )
    ))
