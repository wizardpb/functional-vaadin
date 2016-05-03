(ns functional-vaadin.data-binding.binding-test
  (:require [clojure.test :refer :all]
            [functional-vaadin.data-binding.binding :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import [com.vaadin.ui TextField Label Table ComboBox]
           (functional_vaadin.ui TestUI)
           (com.vaadin.data.util ObjectProperty)
           (com.vaadin.data.fieldgroup FieldGroup)))

(deftest ui-binding
  (testing "Bind default structure"
    (let [ui (TestUI.)
          component (Label.)
          binding (do
                    (bind-component ui :Property component :comp.data)
                    (get-data ui (binding-key :comp.data)))]
      (is (not (nil? binding)))
      (is (= (count (:components binding)) 1))
      (is (identical? (first (vec (:components binding))) component))
      (is (nil? (:binder binding)))
      (is (= (:bind-type binding) :Property))
      (is (= (:structure binding) :Any))
      )
    (let [ui (TestUI.)
          component (FieldGroup.)
          binding (do
                    (bind-component ui :Item component :comp.data)
                    (get-data ui (binding-key :comp.data)))]
      (is (not (nil? binding)))
      (is (= (count (:components binding)) 1))
      (is (identical? (first (vec (:components binding))) component))
      (is (nil? (:binder binding)))
      (is (= (:bind-type binding) :Item))
      (is (= (:structure binding) :Map))
      )
    (let [ui (TestUI.)
          component (ComboBox.)
          binding (do
                    (bind-component ui :Container component :comp.data)
                    (get-data ui (binding-key :comp.data)))]
      (is (not (nil? binding)))
      (is (= (count (:components binding)) 1))
      (is (identical? (first (vec (:components binding))) component))
      (is (nil? (:binder binding)))
      (is (= (:bind-type binding) :Container))
      (is (= (:structure binding) :CollectionAny))
      )
    (let [ui (TestUI.)
          component (Table.)
          binding (do
                    (bind-component ui :Container component :comp.data)
                    (get-data ui (binding-key :comp.data)))]
      (is (not (nil? binding)))
      (is (= (count (:components binding)) 1))
      (is (identical? (first (vec (:components binding))) component))
      (is (nil? (:binder binding)))
      (is (= (:bind-type binding) :Container))
      (is (= (:structure binding) :CollectionMap))
      ))
  )