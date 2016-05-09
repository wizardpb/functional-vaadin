(ns functional-vaadin.event-handling-test
  (:use [clojure.test]
        [functional-vaadin.core]
        [functional-vaadin.event-handling]
        [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.ui Button Panel Image Embedded FormLayout TextField Field$ValueChangeEvent AbstractComponent)
           (com.vaadin.event MouseEvents$ClickEvent)
           (com.vaadin.data Property Property$ValueChangeEvent)
           (com.vaadin.data.util ObjectProperty)))

(defn do-mouse-click [component]
  (.click
    (first (.getListeners component MouseEvents$ClickEvent))
    (MouseEvents$ClickEvent. component nil)))

;(deftest button-events
;  (testing "Firing"
;    (let [clicked (atom false)
;          button (button {:onClick (fn [src evt fg] (swap! clicked #(vector (not %1) fg src)))})]
;      (.click button)
;      (is (first @clicked))
;      (is (nil? (second @clicked)))
;      (.click button)
;      (is (not (first @clicked))))
;    (let [clicked (atom false)
;          ^FormLayout form (form
;                 (button {:onClick (fn [src evt fg] (swap! clicked #(vector (not %1) fg src)))}))
;          ^Button button (.getComponent form 0)
;          field-group (get-field-group form)]
;      (.click button)
;      (is (first @clicked))
;      (is (identical? field-group (second @clicked)))
;      (is (identical? button (nth @clicked 2)))
;      (.click button)
;      (is (not (first @clicked)))
;      (is (identical? field-group (second @clicked)))
;      (is (identical? button (nth @clicked 2)))
;      )))

;(deftest mouse-events
;  (testing "Panel"
;    (let [clicked (atom false)
;          panel (panel {:onClick (fn [comp evt] (swap! clicked #(not %1)))})]
;      (do-mouse-click panel)
;      (is @clicked)
;      (do-mouse-click panel)
;      (is (not @clicked))))
;  (testing "Image"
;    (let [clicked (atom false)
;          image (image {:onClick (fn [comp evt] (swap! clicked #(not %1)))})]
;      (do-mouse-click image)
;      (is @clicked)
;      (do-mouse-click image)
;      (is (not @clicked))))
;  (testing "Embedded"
;    (let [clicked (atom false)
;          embedded (embedded {:onClick (fn [comp evt] (swap! clicked #(not %1)))})]
;      (do-mouse-click embedded)
;      (is @clicked)
;      (do-mouse-click embedded)
;      (is (not @clicked))))
;  )

;(deftest value-change-events
;  (testing "Fields"
;    (let [changed (atom nil)
;          ^TextField field (text-field {:onValueChange
;                                        (fn [comp evt fg]
;                                          (swap! changed (fn [_] (vector (.getValue comp) evt fg)))
;                                          )})]
;      (.setValue field "Text1")
;      (is (vector? @changed))
;      (is (= (first @changed) "Text1"))
;      (is (instance? Field$ValueChangeEvent (second @changed)))
;      (is (nil? (nth @changed 2)))
;      ))
;  (testing "Properties"
;    (let [changed (atom nil)
;          ^Property prop (ObjectProperty. "")
;          ]
;      (onValueChange prop (fn [comp evt]
;                            (swap! changed (fn [_] (vector (.getValue comp) evt)))
;                            ))
;      (.setValue prop "Text1")
;      (is (vector? @changed))
;      (is (= (first @changed) "Text1"))
;      (is (instance? Property$ValueChangeEvent (second @changed)))
;      )))
