(ns functional-vaadin.build-support-test
  (:use [clojure.test]
        [functional-vaadin.build-support]
        [functional-vaadin.utils])
  (:import (com.vaadin.ui Button VerticalLayout Panel Label GridLayout)
           (com.vaadin.server Resource ClassResource)))

(deftest new-instance
  (testing "Ctor choosing"
    (doseq [args [
                  (list "Caption")
                  (list (ClassResource. "file"))
                  (list "Caption" (ClassResource. "file"))
                  (list {:caption "Caption"})
                  ]
            ]
      (let [[b c] (create-widget Button args false)]
        (is (instance? Button b))
        (is (= c '()))
        (is (= (if (instance? Resource (first args)) nil "Caption") (.getCaption b)))))
    (let [[b c] (create-widget Button () false)]
      (is (instance? Button b))
      (is (= c '())))
    (is (thrown-with-msg? IllegalArgumentException #"Cannot create a Button from \(1 2\)"
                          (create-widget Button '(1 2) false)))
    (doseq [args [
                  (list "Caption" :a :b)
                  (list (VerticalLayout.) :a :b)
                  (list "Caption" (VerticalLayout.) :a :b)
                  (list {:caption "Caption" :content (VerticalLayout.)} :a :b)
                  ]
            ]
      (let [[b c] (create-widget Panel args true)]
        (is (instance? Panel b))
        (is (= [:a :b] c))
        (is (= (if (and (instance? VerticalLayout (first args)) (= 3 (count args))) nil "Caption" ) (.getCaption b)))))

    (doseq [args [
                  (list (Label. "Cell 1-1"))
                  (list 1 1 (Label. "Cell 1-1"))
                  ]
            ]
      (let [[b c] (create-widget GridLayout args true)]
        (is (instance? GridLayout b))
        (is (= 1 (count c))))))

  )
