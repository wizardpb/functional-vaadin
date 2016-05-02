(ns functional-vaadin.data-binding.core-test
  (:require [clojure.test :refer :all]
            [functional-vaadin.data-binding.binding :refer :all])
  (:import [com.vaadin.ui TextField]))

(deftest bind-property
  (testing "Fields"
    (let [p (TextField. "" "Inital Text")]
      (do
        (is (= (.getValue p) "Inital Text"))
        (set-component-data p "New Text")
        (is (= (.getValue p) "New Text"))
        ))))
