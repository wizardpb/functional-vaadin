(ns functional-vaadin.config-test
  (:use [clojure.test]
            [functional-vaadin.config])

  (:import (com.vaadin.ui Button VerticalLayout)
           (com.vaadin.shared.ui MarginInfo)
           (com.vaadin.server Sizeable)))

(deftest configuration

  (testing "Single option args"
    (let [obj (Button.)]
      (is (identical? (configure obj {:caption "Caption"}) obj))
      (is (= (.getCaption (configure obj {:caption "Caption2"})) "Caption2"))
      (is (= (.getCaption (configure obj {:caption ["Caption3"]})) "Caption3"))
      (is (= (.getHeight (configure obj {:height "3px"})) 3.0))
      (is (= (.getHeight (configure obj {:height ["3px"]})) 3.0)))

    (let [obj (configure (VerticalLayout.) {:margin true :spacing true })]
      (is (.isSpacing obj))
      (is (= (.getMargin obj) (MarginInfo. true true true true)))))

  (testing "Multiple option args"

    (let [obj (Button.)]
      (is (= (.getHeight (configure obj {:height [3 (Sizeable/UNITS_INCH)]})) 3.0))
      (is (= (.getHeightUnits (configure obj {:height [3 (Sizeable/UNITS_INCH)]})) (Sizeable/UNITS_INCH))))

    (let [obj (configure (Button.) {:height [3 (Sizeable/UNITS_MM)] :width [4 (Sizeable/UNITS_MM)]})]
      (is (= (.getHeight obj) 3.0))
      (is (= (.getHeightUnits obj) (Sizeable/UNITS_MM)))
      (is (= (.getWidth obj) 4.0))
      (is (= (.getWidthUnits obj) (Sizeable/UNITS_MM)))
      ))

  (testing "Error handling"
    (is (thrown-with-msg?
          IllegalArgumentException #"Configuration options must be a Map"
          (configure (Button.) :keyword)))
    (is (thrown-with-msg?
          UnsupportedOperationException #"No such option for class com.vaadin.ui.Button: :wozza"
          (configure (Button.) {:wozza "wizzbang"}))))

  )
