(ns functional-vaadin.data-binding.container-test
  (:require [clojure.test :refer :all]
            [functional-vaadin.data-binding.container :refer :all])
  (:import [com.vaadin.data Item Property Container]))

(deftest db-container
  (testing "Creation - collection of collections"
    (doseq [data [
                  [[0 1 2] [0 1 2]]
                  '((0 1 2) (0 1 2))
                  [#{0 1 2} #{0 1 2}]
                  ]
            ]
      (let [^Container c (->Container data)]
        (is (instance? Container c))
        (is (= (set (.getContainerPropertyIds c)) #{0 1 2}))
        (is (every? (fn [item-id]
                      (let [item (.getItem c item-id)]
                        (every? #(= (.getValue (.getItemProperty item %1)) %1)
                                (.getItemPropertyIds item))))
                    (.getItemIds c))))
      ))
  (testing "Creation - collection of maps"
    (doseq [data [
                  [{0 0, 1 1, 2 2} {0 0, 1 1, 2 2}]
                  '({0 0, 1 1, 2 2} {0 0, 1 1, 2 2})
                  #{{0 0, 1 1, 2 2}}
                  ]
            ]
      (let [^Container c (->Container data)]
        (is (instance? Container c))
        (is (= (set (.getContainerPropertyIds c)) #{0 1 2}))
        (is (every? (fn [item-id]
                      (let [item (.getItem c item-id)]
                        (every? #(= (.getValue (.getItemProperty item %1)) %1)
                                (.getItemPropertyIds item))))
                    (.getItemIds c))))
      ))
  (testing "Creation - map of collections"
    (doseq [data [
                  {0 [0 1 2] 1 [0 1 2]}
                  {0 '(0 1 2) 1 '(0 1 2) }
                  {0 #{0 1 2} 1 #{0 1 2}}
                  ]
            ]
      (let [^Container c (->Container data)]
        (is (instance? Container c))
        (is (= (set (.getContainerPropertyIds c)) #{0 1 2}))
        (is (every? (fn [item-id]
                      (let [item (.getItem c item-id)]
                        (every? #(= (.getValue (.getItemProperty item %1)) %1)
                                (.getItemPropertyIds item))))
                    (.getItemIds c))))
      ))
  (testing "Creation - map of maps"
    (doseq [data [
                  {0 {0 0, 1 1, 2 2} 1 {0 0, 1 1, 2 2}}
                  ]
            ]
      (let [^Container c (->Container data)]
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
                  #{1 2 3}
                  {1 :a 2 :b 3 :c}
                  ]
            ]
      (let [^Container c (->Container data)]
        (is (= (set (.getContainerPropertyIds c)) #{}))
        (is (= (set (.getItemIds c)) #{1 2 3}))
        )
      ))
  (testing "Creation - empty"
    (doseq [data [
                  []
                  '()
                  #{}
                  {}
                  ]
            ]
      (let [^Container c (->Container data)]
        (is (= (set (.getContainerPropertyIds c)) #{}))
        (is (= (set (.getItemIds c)) #{}))
        )
      ))

  )