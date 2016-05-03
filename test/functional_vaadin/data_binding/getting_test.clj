(ns functional-vaadin.data-binding.getting-test
  (:require [clojure.test :refer :all]
            [functional-vaadin.data-binding.binding :refer :all]
            [functional-vaadin.data-binding.getting :refer :all])
  (:import (com.vaadin.data.util ObjectProperty PropertysetItem IndexedContainer)))

(deftest binding-get
  (testing "Any"
    (is (= "Value" (get-bound-value {:binder    (ObjectProperty. "Value")
                                     :structure :Any
                                     :bind-type :Property}))))
  (testing "Map"
    (let [item (PropertysetItem.)]
      (.addItemProperty item :a (ObjectProperty. 1))
      (.addItemProperty item :b (ObjectProperty. 2))
      (is (= {:a 1 :b 2} (get-bound-value {:binder item
                                           :structure :Map
                                           :bind-type :Item})))))
  (testing "CollectionAny"
    (let [container (IndexedContainer. (vec (range 0 10)))]
      (is (= (vec (range 0 10)) (get-bound-value {:binder    container
                                                  :structure :CollectionAny
                                                  :bind-type :Container}))))
    )
  (testing "CollectionMap"
    (let [container (IndexedContainer. (vec (range 0 10)))]
      (.addContainerProperty container :a Object nil)
      (.addContainerProperty container :b Object nil)
      (doseq [id (range 0 10)]
        (.setValue (.getContainerProperty container id :a) (str id ",1"))
        (.setValue (.getContainerProperty container id :b) (str id ",2")))
      (is (= (map #(hash-map :a (str %1 ",1") :b (str %1 ",2")) (range 0 10))
             (get-bound-value {:binder    container
                               :structure :CollectionMap
                               :bind-type :Container})))))
  (testing "Unset bindings"
    (doseq [binding [{:structure :Any
                      :bind-type :Property}
                     {:structure :Map
                      :bind-type :Item}
                     {:structure :CollectionAny
                      :bind-type :Container}
                     {:structure :CollectionMap
                      :bind-type :Container}]
            ] (is (nil?
           (get-bound-value binding)))))
  )
