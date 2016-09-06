(ns functional-vaadin.data-binding-test
  (:use [clojure.test]
        [functional-vaadin.core]
        [functional-vaadin.data-binding])
  (:import (com.vaadin.data.util HierarchicalContainer)
           (com.vaadin.ui TreeTable)
           (com.vaadin.data Container$Hierarchical)))

(deftest hierarchical
  (testing "Adding - select list"
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
  (testing "Adding - table row"
    (doseq [c [
               (TreeTable.)
               (HierarchicalContainer.)
               ]]
      (let [container (add-hierarchy
                       (doto c
                         (.addContainerProperty "Col1" String nil)
                         (.addContainerProperty "Col2" String nil)
                         (.addContainerProperty "Col3" String nil)
                         )
                       [{["Row11"]
                         [["Row21" "Row22" "Row23"]
                          ["Row31" "Row32" "Row33"]]}
                        {["Row41"]
                         [["Row51" "Row52" "Row53"]
                          {["Row61"]
                           [["Row71" "Row72" "Row73"]
                            ["Row81" "Row82" "Row83"]]}]}
                        ])]
       (is (instance? Container$Hierarchical container))
       (is (= (set (.rootItemIds container)) #{0 3}))
       (is (every? (fn [[c p]] (= (.getParent container c) p))
             [[0 nil] [3 nil]
              [1 0] [2 0]
              [4 3] [5 3]
              [6 5] [7 5]
              ]))
       (is (every? (fn [[id row]]
                     (= (map #(.getValue (.getContainerProperty container id %1)) (.getContainerPropertyIds container))
                       row))
             (map-indexed #(vector %1 %2)
               [["Row11" nil nil]
                ["Row21" "Row22" "Row23"]
                ["Row31" "Row32" "Row33"]
                ["Row41" nil nil]
                ["Row51" "Row52" "Row53"]
                ["Row61" nil nil]
                ["Row71" "Row72" "Row73"]
                ["Row81" "Row82" "Row83"]
                ])))
       )))
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
      ))
  )
