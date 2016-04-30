(ns functional-vaadin.event-handling-test
  (:use [clojure.test]
        [functional-vaadin.core]
        [functional-vaadin.event-handling]
        [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.ui Button Panel Image Embedded FormLayout)
           (com.vaadin.event MouseEvents$ClickEvent)))

;; TODO - replace with FRP - which one?

(defn do-mouse-click [component]
  (.click
    (first (.getListeners component MouseEvents$ClickEvent))
    (MouseEvents$ClickEvent. component nil)))

(deftest button-events
  (testing "Firing"
    (let [clicked (atom false)
          button (button {:onClick (fn [evt fg] (swap! clicked #(vector (not %1) fg)))})]
      (.click button)
      (is (first @clicked))
      (is (nil? (second @clicked)))
      (.click button)
      (is (not (first @clicked))))
    (let [clicked (atom false)
          ^FormLayout form (form
                 (button {:onClick (fn [evt fg] (swap! clicked #(vector (not %1) fg)))}))
          ^Button button (.getComponent form 0)
          field-group (get-data form :field-group)]
      (.click button)
      (is (first @clicked))
      (is (identical? field-group (second @clicked)))
      (.click button)
      (is (not (first @clicked)))
      (is (identical? field-group (second @clicked)))
      )))

(deftest mouse-events
  (testing "Panel"
    (let [clicked (atom false)
          panel (panel {:onClick (fn [evt] (swap! clicked #(not %1)))})]
      (do-mouse-click panel)
      (is @clicked)
      (do-mouse-click panel)
      (is (not @clicked))))
  (testing "Image"
    (let [clicked (atom false)
          image (image {:onClick (fn [evt] (swap! clicked #(not %1)))})]
      (do-mouse-click image)
      (is @clicked)
      (do-mouse-click image)
      (is (not @clicked))))
  (testing "Embedded"
    (let [clicked (atom false)
          embedded (embedded {:onClick (fn [evt] (swap! clicked #(not %1)))})]
      (do-mouse-click embedded)
      (is @clicked)
      (do-mouse-click embedded)
      (is (not @clicked))))
  )

(deftest field-events
  ())