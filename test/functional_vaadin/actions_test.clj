;;
;; Copyright 2016 Prajna Inc. All Rights Reserved.
;;
;; This work is licensed under the Eclipse Public License v1.0 - https://www.eclipse.org/legal/epl-v10.html
;; Distrubition and use must be done under the terms of this license
;;

(ns functional-vaadin.actions-test
  (:use [clojure.test]
        [functional-vaadin.actions]
        )
  (:import (com.vaadin.event ShortcutAction ShortcutAction$KeyCode Action Action$Listener Action$Handler)
           (com.vaadin.server FileResource)
           (java.io File)
           (functional_vaadin.actions ActionHandler)))

(deftest creation-fns
  (testing "ShortcutActions"
    (let [result (atom nil)
          a (->ShortcutAction "Enter" ShortcutAction$KeyCode/ENTER (fn [a s t] (swap! result (fn [_] [a s t]))))]
      (is (instance? ShortcutAction a))
      (is (instance? Action$Listener a))
      (is (= (.getCaption a) "Enter"))
      (is (not (.getIcon a)))
      (is (= (.getKeyCode a) ShortcutAction$KeyCode/ENTER))
      (do
        (.handleAction a "Sender" "Target")
        (is (= @result [a "Sender" "Target"])))
      )
    (let [result (atom nil)
          icon (FileResource. (File. "icon"))
          a (->ShortcutAction ["Enter" icon] ShortcutAction$KeyCode/ENTER (fn [a s t] (swap! result (fn [_] [a s t]))))]
      (is (instance? ShortcutAction a))
      (is (instance? Action$Listener a))
      (is (= (.getCaption a) "Enter"))
      (is (= (.getIcon a) icon))
      (is (= (.getKeyCode a) ShortcutAction$KeyCode/ENTER))
      (do
        (.handleAction a "Sender" "Target")
        (is (= @result [a "Sender" "Target"])))
      )
    )
  (testing "Actions"
    (let [result (atom nil)
          icon (FileResource. (File. "icon"))
          a (->FunctionAction "Mark" icon (fn [a s t] (swap! result (fn [_] [a s t]))))]
      (is (instance? Action a))
      (is (instance? Action$Listener a))
      (is (= (.getCaption a) "Mark"))
      (is (= (.getIcon a) icon))
      (do
        (.handleAction a "Sender" "Target")
        (is (= @result [a "Sender" "Target"])))
      ))
  (testing "ActionHandler"
    (let [result (atom nil)
          icon (FileResource. (File. "icon"))
          actions (map #(->FunctionAction (str "Action " %1) icon (fn [a s t] (swap! result (fn [_] [a s t])))) (range 10))
          ah (->ActionHandler all-actions dispatch-listener actions)]
      (is (instance? ActionHandler ah))
      (is (instance? Action$Handler ah))
      (is (= (seq (.getActions ah "Target" "Sender")) actions))
      (let [a (first (.getActions ah "Target" "Sender"))]
        (.handleAction a "Sender" "Target")
        (is (= @result [a "Sender" "Target"])))
      ))
  )