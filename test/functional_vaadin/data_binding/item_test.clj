(ns functional-vaadin.data-binding.item-test
  (:require [clojure.test :refer :all]
            [functional-vaadin.data-binding.item :refer :all])
  (:import [com.vaadin.data Item Property]))

(deftest db-item
  (testing "Creation"
    (doseq [c [{:a 0 :b 1} [0 1] #{0 1} '(0 1)]]
      (let [item (->Item c)]
        (is (instance? Item item))
        (is (every? #(instance? Property %1)
                    (map #(.getItemProperty item %1) (.getItemPropertyIds item)))))))
  (testing "Property creation and access"
    (doseq [c [[0 1] #{0 1} '(0 1)]]
      (let [item (->Item c)]
        (is (= (set (.getItemPropertyIds item)) #{0 1}))
        (is (every? (fn [[id val]] (= id val))
                    (map #(vector %1 (.getValue (.getItemProperty item %1)))
                         (.getItemPropertyIds item)))))
      ))
  )
