(ns functional-vaadin.rx.operators-test
  (:require [clojure.test :refer :all]
            [rx.lang.clojure.core :as rx]
            [functional-vaadin.core :refer :all]
            [functional-vaadin.rx.observers :refer :all]
            [functional-vaadin.rx.operators :refer :all]
            [functional-vaadin.utils :refer :all]
            )
  (:import (com.vaadin.ui Button$ClickEvent)
           (java.util Map)))

(deftest rx-operators
  (testing "Commit"
    (let [form (form (button))
          b (.getComponent form 0)
          fired (atom nil)]
      (rx/subscribe
        (->> (button-clicks b)
            (commit))
        (fn [v] (swap! fired (fn [_] v))))
      (.click b)
      (is (instance? Map @fired))
      (is (identical? (:source @fired) b))
      (is (instance? Button$ClickEvent (:event @fired)))
      (is (identical? (get-field-group form) (:field-group @fired)))
      (is (identical? (.getItemDataSource (:field-group @fired)) (:item @fired)))
      )
    )
  (testing "Consume-for"
    (let [b (button) l (label) fired (atom nil)]
      (->> (button-clicks b)
           (consume-for l (fn [l v] (swap! fired (fn [_] {:component l :value v})))))
      (fn [v] (swap! fired (fn [_] v)))
      (.click b)
      (is (identical? (:component @fired) l))
      (is (= (keys (:value @fired)) [:source :event :field-group]))
      (is (identical? b (get-in @fired [:value :source])))
      (is (nil? (get-in @fired [:value :field-group])))
      )
    )
  (testing "with-ui-access"
    )
  )
