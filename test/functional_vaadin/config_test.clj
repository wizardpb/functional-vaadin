(ns functional-vaadin.config-test
  (:require [clojure.spec :as s])
  (:use [clojure.test]
        [functional-vaadin.core]
        [functional-vaadin.actions]
        [functional-vaadin.validation]
        [functional-vaadin.naming]
        [functional-vaadin.thread-vars]
        [functional-vaadin.config]
        [functional-vaadin.utils]
        )

  (:import (com.vaadin.ui Button VerticalLayout Alignment TextField Label)
           (com.vaadin.shared.ui MarginInfo)
           (com.vaadin.server Sizeable)
           (java.util Map)
           (functional_vaadin.ui TestUI)
           (com.vaadin.data.util PropertysetItem)
           (com.vaadin.data.fieldgroup FieldGroup)
           (com.vaadin.data.validator StringLengthValidator)
           (functional_vaadin.validation FunctionalValidator)
           (functional_vaadin.ui LoginForm)
           (com.vaadin.event ActionManager Action$Handler Action$Listener)))

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

    (let [f (text-field {:validateWith (StringLengthValidator. "Length Error: {0}")})]
      (is (= 1 (count (.getValidators f))))
      (is (instance? StringLengthValidator (first (.getValidators f)))))

    (let [f (text-field {:validateWith [
                                        (StringLengthValidator. "Length Error: {0}")
                                        (->FunctionalValidator (fn [obj] false) "Always fail {0}")]})]
      (is (= 2 (count (.getValidators f))))
      (is (= [StringLengthValidator FunctionalValidator] (map #(class %1) (.getValidators f)))))
    )

  (testing "Actions"
    (is (instance? Action$Handler (->ActionHandler all-actions dispatch-listener
                                    [(->FunctionAction "Action" (fn [a s t] ))])))
    (is (instance? Action$Listener (->FunctionAction "Action" (fn [a s t] ))))
    (is (every? #(instance? Action$Listener %)
          (map
            #(->FunctionAction (str "Action" %) (fn [a s t]))
            (range 5))))
    (let [results (atom nil)
          actions (map
                    #(->FunctionAction (str "Action" %) (fn [a s t] (swap! results (fn [_] [a s t]))))
                    (range 5))
          ^ActionManager am (action-manager {:actions actions})
          ]
      (is (= (seq (.getActions am "Target" "Sender")) actions))
      (let [a (first (.getActions am "Target" "Sender"))]
        (.handleAction a "Sender" "Target")
        (is (= @results [a "Sender" "Target"])))
      )
    (let [results (atom nil)
          actions (map
                    #(->FunctionAction (str "Action" %) (fn [a s t] (swap! results (fn [_] [a s t]))))
                    (range 5))
          ^ActionManager am (action-manager {:actions (->ActionHandler all-actions dispatch-listener actions)})
          ]
      (is (= (seq (.getActions am "Target" "Sender")) actions))
      (let [a (first (.getActions am "Target" "Sender"))]
        (.handleAction a "Sender" "Target")
        (is (= @results [a "Sender" "Target"]))))
    (let [results (atom nil)
          actions (vec (map
                     #(->FunctionAction (str "Action" %) (fn [a s t] (swap! results (fn [_] [a s t]))))
                     (range 5)))
          ^ActionManager am (action-manager {:actions (->ActionHandler all-actions dispatch-listener actions)})
          ]
      (is (= (seq (.getActions am "Target" "Sender")) actions))
      (let [a (first (.getActions am "Target" "Sender"))]
        (.handleAction a "Sender" "Target")
        (is (= @results [a "Sender" "Target"]))))
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

  (testing "Computed children"
    (let [l (apply vertical-layout (map #(label (str "Label " %1)) (range 0 10)))]
      (is (= (.getComponentCount l) 10))
      (is (every? #(instance? Label %1) (map #(.getComponent l %1) (range (.getComponentCount l)))))
      (is (every? true? (map #(= (.getValue (.getComponent l %1)) (str "Label " %1 )) (range (.getComponentCount l)))))

      )
    (let [l (apply vertical-layout {:margin true :spacing true} (map #(label (str "Label " %1)) (range 0 10)))]
      (is (.getMargin l))
      (is (.isSpacing l))
      (is (= (.getComponentCount l) 10))
      (is (every? #(instance? Label %1) (map #(.getComponent l %1) (range (.getComponentCount l)))))
      (is (every? true? (map #(= (.getValue (.getComponent l %1)) (str "Label " %1 )) (range (.getComponentCount l)))))

      )
    )

  (testing "Error handling"
    (is (thrown-with-msg?
          IllegalArgumentException #"Configuration options must be a Map"
          (configure (Button.) :keyword)))
    (is (thrown-with-msg?
          UnsupportedOperationException #"No such option for class com.vaadin.ui.Button: :wozza"
          (configure (Button.) {:wozza "wizzbang"}))))



  )

(deftest login-form-ui
  (testing "building"
    (let [c (login-form identity)]
      (is (instance? LoginForm c)))
    (let [c (login-form {:usernameCaption "Enter Username"} identity)]
      (is (instance? LoginForm c)))
    (is (thrown-with-msg?
          IllegalArgumentException #"No arguments supplied to login-form"
          (login-form)))))
