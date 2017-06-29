;;
;; Copyright 2016 Prajna Inc. All Rights Reserved.
;;
;; This work is licensed under the Eclipse Public License v1.0 - https://www.eclipse.org/legal/epl-v10.html
;; Distrubition and use must be done under the terms of this license
;;

(ns functional-vaadin.rx.observers-test
  (:require [clojure.test :refer :all]
            [rx.lang.clojure.core :as rx]
            [functional-vaadin.core :refer :all]
            [functional-vaadin.rx.observers :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (java.util Map)
           (com.vaadin.ui Button Button$ClickEvent FormLayout TextField Label$ValueChangeEvent Field$ValueChangeEvent)))

(deftest rx-button
  (testing "Creation/subscribe"
    (let [^Button b (button)
          fired (atom nil)]
      (-> (button-clicks b)
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
      (-> (button-clicks b)
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
      (-> (value-changes field)
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
      (-> (value-changes field)
          (rx/subscribe (fn [v] (swap! fired (fn [_] v)))))
      (.setValue field "New Text")
      (is (instance? Map @fired))
      (is (identical? (:source @fired) field))
      (is (instance? Field$ValueChangeEvent (:event @fired)))
      (is (identical? (get-field-group form) (:field-group @fired)))
      )
    ))

(deftest rx-events-in
  (let [result (atom [])
        o (events-in
            (fn [s end]
              (loop [i 0]
                (when (< i end)
                  (rx/on-next s i)
                  (Thread/sleep 100)
                  (recur (inc i))))) 10)
        sub (rx/subscribe o (fn [v] (swap! result #(conj %1 v))))]
    (loop [unsub (rx/unsubscribed? sub)]
      (Thread/sleep 50)
      (if (not unsub)
        (recur (rx/unsubscribed? sub))))
    (is (= @result (vec (range 0 10))))))
