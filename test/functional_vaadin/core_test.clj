(ns functional-vaadin.core-test
  (:use [clojure.test])
  (:require [functional-vaadin.core :refer :all]
            [functional-vaadin.builders  :refer :all]
            [functional-vaadin.mock-data-provider :refer :all])
  (:import (functional_vaadin.mock_data_provider UIDataProvider)
           (com.vaadin.ui VerticalLayout Label TextField)))

(deftest ui-building
  (testing "Basic UI"
    (let [ui (defui (->UIDataProvider {} nil)
                    (vertical-layout
                      (label "Label 1")
                      (label "Label 2")))]
      (is (instance? UIDataProvider ui))
      (let [vl (.getContent ui)]
        (is (instance? VerticalLayout vl))
        (is (= (.getComponentCount vl) 2))
        (is (every? #(instance? Label %1) (map #(.getComponent vl %1) [0 1])))
        )))
  (testing "Complex UI"
    (let [ui (defui (->UIDataProvider {} nil)
                    (panel "Top Panel"
                           (tab-sheet
                             (vertical-layout {:caption "Tab 1"}
                                              (label "Line 1") (label "Line 2")
                                              (label "Line 3") (label "Line 4")
                                              (label "Line 5") (label "Line 6")
                                              (label "Line 7") (label "Line 8")
                                              (label "Line 9") (label "Line 10")
                                              )
                             (panel "Tab 2"
                                    (form
                                      (form-field "name" TextField)
                                      (form-field "address1" TextField)
                                      (form-field "address2" TextField)
                                      (form-field "city" TextField)
                                      (form-field "state" TextField)))
                             (panel "Tab 3"
                                    (grid-layout 3 4
                                                 (label "R1C1") (label "R1C2") (label "R1C3")
                                                 (label "R2C1") (label "R2C2") (label "R2C3")
                                                 (label "R3C1") (label "R3C2") (label "R3C3")
                                                 (label "R4C1") (label "R4C2") (label "R4C3")
                                                 )
                                    )
                             )
                           )
                    )
          ]
      )))