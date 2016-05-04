(ns functional-vaadin.data-binding.setting-test
  (:require [clojure.test :refer :all]
            [functional-vaadin.data-binding.binding :refer :all]
            [functional-vaadin.data-binding.setting :refer :all])
  (:import [com.vaadin.data Item Property Container]
           (com.vaadin.data.fieldgroup FieldGroup)
           (com.vaadin.ui Table ComboBox TextField)))

(deftest db-property
  (testing "Creation"
    (let [property (:binder (update-binder
                              {:bind-type  :Property
                               :structure  :Any
                               :components #{(TextField.)}
                               }
                              "Some text"))]
      (is (instance? Property property))
      (is (= (.getValue property) "Some text"))
      ))
  )

(deftest db-item
  (testing "Creation"
    (let [item (:binder (update-binder
                          {:bind-type  :Item
                           :structure  :Map
                           :components #{(FieldGroup.)}
                           }
                          {:a 0 :b 1}))]
      (is (instance? Item item))
      (is (every? #(instance? Property %1)
                  (map #(.getItemProperty item %1) (.getItemPropertyIds item))))))

  (testing "Property values"
    (let [item (:binder (update-binder
                         (->Binding {:bind-type  :Item
                                     :structure  :Map
                                     :components #{(FieldGroup.)}})
                         {1 1 2 2}))]
     (is (= (set (.getItemPropertyIds item)) #{1 2}))
     (is (every? (fn [[id val]] (= id val))
                 (map #(vector %1 (.getValue (.getItemProperty item %1)))
                      (.getItemPropertyIds item))))))
  )

(deftest db-container
  (testing "Creation"
    (doseq [data [
                  [{0 0, 1 1, 2 2} {0 0, 1 1, 2 2}]
                  '({0 0, 1 1, 2 2} {0 0, 1 1, 2 2})
                  ]
            ]
      (let [^Container c (:binder (update-binder
                            (->Binding {:bind-type  :Container
                                        :components #{(Table.)}
                                        :structure  :CollectionMap})
                            data))]
        (is (instance? Container c))
        (is (= (set (.getContainerPropertyIds c)) #{0 1 2}))
        (is (every? (fn [item-id]
                      (let [item (.getItem c item-id)]
                        (every? #(= (.getValue (.getItemProperty item %1)) %1)
                                (.getItemPropertyIds item))))
                    (.getItemIds c))))
      ))


  (testing "Creation - itemsIds only"
    (doseq [data [
                  [ 1 2 3]
                  '(1 2 3)
                  ]
            ]
      (let [^Container c (:binder (update-binder
                            (->Binding {:bind-type  :Container
                                        :components #{(ComboBox.)}
                                        :structure  :CollectionAny})
                            data))]
        (is (= (set (.getContainerPropertyIds c)) #{}))
        (is (= (set (.getItemIds c)) #{1 2 3}))
        )
      ))
  (testing "Creation - empty CollectionAny"
    (doseq [data [
                  []
                  '()
                  #{}
                  ]
            ]
      (let [^Container c (:binder (update-binder
                                    (->Binding {:bind-type  :Container
                                                :components #{(ComboBox.)}
                                                :structure  :CollectionAny})
                                    data))]
        (is (= (set (.getContainerPropertyIds c)) #{}))
        (is (= (set (.getItemIds c)) #{}))
        )
      ))
  (testing "Creation - empty CollectionMap"
    (doseq [data [
                  []
                  '()
                  #{}
                  ]
            ]
      (let [^Container c (:binder (update-binder
                                    (->Binding {:bind-type  :Container
                                                :components #{(Table.)}
                                                :structure  :CollectionMap})
                                    data))]
        (is (= (set (.getContainerPropertyIds c)) #{}))
        (is (= (set (.getItemIds c)) #{}))
        )
      ))
  (testing "Creation - nil"
    (doseq [data [
                  []
                  '()
                  #{}
                  ]
            ]
      (let [^Container c (:binder (update-binder
                                    (->Binding {:bind-type  :Container
                                                :components #{(Table.)}
                                                :structure  :CollectionMap})
                                    data))]
        (is (= (set (.getContainerPropertyIds c)) #{}))
        (is (= (set (.getItemIds c)) #{}))
        )
      ))
  )