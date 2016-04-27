(ns functional-vaadin.event-handling-test
  (:use [clojure.test]
        [functional-vaadin.builders]
        [functional-vaadin.event-handling])
  (:import (com.vaadin.ui Button Panel Image Embedded)
           (com.vaadin.event MouseEvents$ClickEvent)))

(defn do-mouse-click [component]
  (.click
    (first (.getListeners component MouseEvents$ClickEvent))
    (MouseEvents$ClickEvent. component nil)))

(deftest button-events
  (testing "Firing"
    (let [clicked (atom false)
          button (button {:onClick (fn [evt] (swap! clicked #(not %1)))})]
      (.click button)
      (is @clicked)
      (.click button)
      (is (not @clicked)))))

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