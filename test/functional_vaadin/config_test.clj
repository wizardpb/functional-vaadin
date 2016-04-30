(ns functional-vaadin.config-test
  (:use [clojure.test]
        [functional-vaadin.thread-vars]
        [functional-vaadin.config]
        [functional-vaadin.core]
        [functional-vaadin.utils]
        [functional-vaadin.mock-data-provider])

  (:import (com.vaadin.ui Button VerticalLayout)
           (com.vaadin.shared.ui MarginInfo)
           (com.vaadin.server Sizeable)))

(deftest configuration

  (testing "Option and data extraction"
    (let [b (configure (Button.) {:caption "Push Me" :expandRatio 0.5 :position [1 1]})]
      (is (= (get-data b :parent-data) {:position [1 1]}))
      (is (= (get-data b :parent-config) {:expandRatio [b 0.5]}))
      (is (= (.getCaption b) "Push Me")))
    (doseq [opt [:position :span]]
      (doseq [vals [nil 1 [1] [1 nil] [:a :b]]]
        (is (thrown?
              IllegalArgumentException                       ;(re-pattern (get-in parent-data-specs [opt :error-msg]))
              (configure (Button.) {opt vals}))))))

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

  (testing "Special attributes"
    (with-bindings
      {#'*current-ui* (->UIDataProvider {} nil)}
      (let [b (button {:id "myform"})]
       (is (= (.getId b) "myform"))
       (is (identical? (.component-at *current-ui* "myform") b))
       (is (identical? (.component-at *current-ui* :myform) b))
       (is (identical? (.component-at *current-ui* [:myform]) b))
       (is (identical? (.component-at *current-ui* ["myform"]) b))
       ))
    (with-bindings
      {#'*current-ui* (->UIDataProvider {} nil)}
      (let [b (button {:id "myform.save"})]
        (is (= (.getId b) "myform.save"))
        (is (identical? (.component-at *current-ui* "myform.save") b))
        (is (identical? (.component-at *current-ui* :myform.save) b))
        (is (identical? (.component-at *current-ui* [:myform :save]) b))
        (is (identical? (.component-at *current-ui* ["myform" "save"]) b))
        ))

    )

  )
