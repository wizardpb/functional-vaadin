(ns functional-vaadin.event-handling-test
  (:use [clojure.test]
        [functional-vaadin.event-handling])
  (:import (com.vaadin.ui Button)))

(deftest button-events
  (testing "Firing"
    (let [button (Button.)
          clicked (atom false)]
      (.addClickListener button (->ButtonClickDelegator (fn [evt] (swap! clicked #(not %1)))))
      (.click button)
      (is @clicked)
      (.click button)
      (is (not @clicked)))))