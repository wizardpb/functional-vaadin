(ns functional-vaadin.build-support-test
  (:use [clojure.test]
        [functional-vaadin.build-support]
        [functional-vaadin.utils])
  (:import (com.vaadin.ui Button VerticalLayout Panel Label GridLayout Table)
           (com.vaadin.server Resource ClassResource)))

(deftest new-instance

  (testing "Ctor choosing"
    (doseq [args [
                  (list "Caption")
                  (list (ClassResource. "file"))
                  (list "Caption" (ClassResource. "file"))
                  (list {:caption "Caption"})
                  (list "Caption" {:icon (ClassResource. "file")})
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
                  (list (VerticalLayout.) (Label. "a") (Label. "b"))
                  (list "Caption" (VerticalLayout.) (Label. "a") (Label. "b"))
                  (list {:caption "Caption" :content (VerticalLayout.)} (Label. "a") (Label. "b"))
                  ]
            ]
      (let [[b c] (create-widget Panel args true)]
        (is (instance? Panel b))
        (is (collection? c))
        (is (= 2 (count c)))
        (is (every? #(instance? Label %1) c))
        (is (= ["a" "b"] (map (fn [it] (.getValue it)) c)))
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


