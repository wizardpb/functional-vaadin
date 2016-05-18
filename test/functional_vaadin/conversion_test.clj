(ns functional-vaadin.conversion-test
  (:use [clojure.test]
        [functional-vaadin.conversion])
  (:import (com.vaadin.data.util HierarchicalContainer)))

(deftest hierarchical
  (testing "Adding"
    (let [container (add-hierarchy (HierarchicalContainer.)
                      [{"Parent1"
                        ["Leaf11" {"Parent21"
                                   ["Leaf12" "Leaf22"]}]}
                       {"Parent2"
                        []}
                       {"Parent3"
                        ["Leaf13" "Leaf23" "Leaf33"]}])]
      (is (instance? HierarchicalContainer container))
      (is (= (set (.rootItemIds container)) #{"Parent1" "Parent2" "Parent3"}))
      (is (every? (fn [[c p]] (= (.getParent container c) p))
            [["Parent1" nil] ["Parent2" nil] ["Parent2" nil]
             ["Leaf11" "Parent1"] ["Parent21" "Parent1"]
             ["Leaf12" "Parent21"] ["Leaf22" "Parent21"]
             ["Leaf13" "Parent3"] ["Leaf23" "Parent3"] ["Leaf33" "Parent3"]
             ]))
      ))
  (testing "Creating"
    (let [container (->Hierarchical
                      [{"Parent1"
                        ["Leaf11" {"Parent21"
                                   ["Leaf12" "Leaf22"]}]}
                       {"Parent2"
                        []}
                       {"Parent3"
                        ["Leaf13" "Leaf23" "Leaf33"]}])]
      (is (instance? HierarchicalContainer container))
      (is (= (set (.rootItemIds container)) #{"Parent1" "Parent2" "Parent3"}))
      (is (every? (fn [[c p]] (= (.getParent container c) p))
            [["Parent1" nil] ["Parent2" nil] ["Parent2" nil]
             ["Leaf11" "Parent1"] ["Parent21" "Parent1"]
             ["Leaf12" "Parent21"] ["Leaf22" "Parent21"]
             ["Leaf13" "Parent3"] ["Leaf23" "Parent3"] ["Leaf33" "Parent3"]
             ]))
      )))
