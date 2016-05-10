(ns functional-vaadin.config-test
  (:use [clojure.test]
        [functional-vaadin.naming]
        [functional-vaadin.thread-vars]
        [functional-vaadin.config]
        [functional-vaadin.core]
        [functional-vaadin.utils]
        )

  (:import (com.vaadin.ui Button VerticalLayout Alignment TextField)
           (com.vaadin.shared.ui MarginInfo)
           (com.vaadin.server Sizeable)
           (java.util Map)
           (functional_vaadin.ui TestUI)
           (com.vaadin.data.util PropertysetItem)
           (com.vaadin.data.fieldgroup FieldGroup)))

(defmacro with-form [& forms]
  `(with-bindings
     {#'*current-field-group* (FieldGroup. (PropertysetItem.))}
     ~@forms
     *current-field-group*))

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

  (testing "Parent attributes"
    )

  (testing "Synthetic attributes"
    (with-bindings
      {#'*current-ui* (TestUI.)}
      (let [b (button {:id "myform"})]
        (is (= (.getId b) "myform"))
        (is (identical? (componentAt *current-ui* :myform) b))
        ))
    (with-bindings
      {#'*current-ui* (TestUI.)}
      (let [b (button {:id :myform.save})]
        (is (= (.getId b) "myform.save"))
        (is (identical? (componentAt *current-ui* :myform.save) b))
        ))
    (let [b (button {:position [0 0]})]
      (is (= (.getData b) {:parent-data {:position [0 0]}})))
    (let [b (button {:alignment Alignment/TOP_LEFT})]
      (is (= (.getData b) {:parent-data {:componentAlignment [b Alignment/TOP_LEFT]}}))
      )
    (button {:addStyleName "border"})                       ;How to verify ?
    )

  (testing "Binding"
    (let [fg (with-form
               (bind-field (TextField.) "propId"))
          item (.getItemDataSource fg)]
      (is (= (set (.getItemPropertyIds item)) #{"propId"}))
      (is (identical? (.getType (.getItemProperty item "propId")) Object))
      (is (nil? (.getValue (.getItemProperty item "propId"))))
      )
    (doseq [bval [["propId" String]
                  {:propertyId "propId" :type String}]]
      (let [fg (with-form
                (bind-field (TextField.) bval))
           item (.getItemDataSource fg)]
       (is (= (set (.getItemPropertyIds item)) #{"propId"}))
       (is (identical? (.getType (.getItemProperty item "propId")) String))
       (is (nil? (.getValue (.getItemProperty item "propId"))))
       ))
    (doseq [bval [["propId" String "text"]
                  {:propertyId "propId" :type String :initialValue "text"}]]
      (let [fg (with-form
                (bind-field (TextField.) bval))
           item (.getItemDataSource fg)]
       (is (= (set (.getItemPropertyIds item)) #{"propId"}))
       (is (identical? (.getType (.getItemProperty item "propId")) String))
       (is (= (.getValue (.getItemProperty item "propId")) "text"))
       ))

    )

  (testing "Margin setting"
    (let [l (vertical-layout {:margin true})]
      (is (= (.getMargin l) (MarginInfo.  true ))))
    (let [l (vertical-layout {:margin [:vertical]})]
      (is (= (.getMargin l) (MarginInfo.  true false))))
    (let [l (vertical-layout {:margin [:horizontal]})]
      (is (= (.getMargin l) (MarginInfo. false true))))
    (let [l (vertical-layout {:margin [:vertical :top]})]
      (is (= (.getMargin l) (MarginInfo. true false))))
    (let [l (vertical-layout {:margin [:vertical :left]})]
      (is (= (.getMargin l) (MarginInfo. true false true true))))
    (let [l (vertical-layout {:margin [:horizontal :left]})]
      (is (= (.getMargin l) (MarginInfo. false true))))
    (let [l (vertical-layout {:margin [:top :left]})]
      (is (= (.getMargin l) (MarginInfo. true false false true))))
    )

  (testing "Error handling"
    (is (thrown-with-msg?
          IllegalArgumentException #"Configuration options must be a Map"
          (configure (Button.) :keyword)))
    (is (thrown-with-msg?
          UnsupportedOperationException #"No such option for class com.vaadin.ui.Button: :wozza"
          (configure (Button.) {:wozza "wizzbang"}))))



  )
