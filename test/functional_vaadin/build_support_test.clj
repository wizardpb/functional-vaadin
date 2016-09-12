(ns functional-vaadin.build-support-test
  (:require [clojure.spec :as s])
  (:use [clojure.test]
        [functional-vaadin.build-support]
        [functional-vaadin.utils])
  (:import (com.vaadin.ui Button VerticalLayout Panel Label GridLayout Table)
           (com.vaadin.server Resource ClassResource FileResource)
           (clojure.lang MapEntry)
           (java.io File)))

(deftest component-args-spec
  (testing "conformance"
    (is (= (s/conform ::functional-vaadin.build-support/component-args '())
          {}))
    (is (= (s/conform ::functional-vaadin.build-support/component-args [{:hidden true}])
          {:config {:hidden true}}))
    (is (= (s/conform ::functional-vaadin.build-support/component-args '("c1" "c2"))
          {:initial-args ["c1" "c2"]}))
    (is (= (s/conform ::functional-vaadin.build-support/component-args '("c1" "c2" {:hidden true}))
          {:initial-args ["c1" "c2"] :config {:hidden true}}))
    (let [c1 (Button.)
          c2 (VerticalLayout.)]
      (is (= (s/conform ::functional-vaadin.build-support/component-args (list {:hidden true} c1 c2))
            {:config {:hidden true} :children [c1 c2]}))
      (is (= (s/conform ::functional-vaadin.build-support/component-args (list "c1" c1 {:hidden true} c1 c2))
            {:initial-args ["c1" c1] :config {:hidden true} :children [c1 c2]})))
    )
  (testing "failed conformance"
    (is (= (s/conform ::functional-vaadin.build-support/component-args [{:hidden true} {:enabled false}])
          ::s/invalid))
    (let [c1 (Button.)
          c2 (VerticalLayout.)]
      (is (= (s/conform ::functional-vaadin.build-support/component-args ["c1" {:hidden true} {:enabled false} c1])
            ::s/invalid))))
  )

(deftest menu-item-spec
  (testing "fn spec"
    (let [icon (FileResource. (File. "fname"))
          ispec1 (->MenuItemSpec "sub1" nil identity)
          ispec2 (->MenuItemSpec "sub2" nil identity)]
      (is (=
            (s/conform ::functional-vaadin.build-support/menu-item-args (list "item" identity))
            {:name "item" :children (MapEntry. :item_fn identity)}))
      (is (=
            (s/conform ::functional-vaadin.build-support/menu-item-args (list "item" icon identity))
            {:name "item" :icon_resource icon :children (MapEntry. :item_fn identity)}))
      (is (=
            (s/conform ::functional-vaadin.build-support/menu-item-args (list "item" ispec1 ispec2))
            {:name "item" :children (MapEntry. :sub_items [ispec1 ispec2])}))
      (is (=
            (s/conform ::functional-vaadin.build-support/menu-item-args (list "item"))
            ::s/invalid)))
    ))

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
    (is (thrown-with-msg? IllegalArgumentException #"Cannot create a Button from \[1 2\]"
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
                  (list 1 1 (Label. "Cell 1-1"))
                  ]
            ]
      (let [[b c] (create-widget GridLayout args true)]
        (is (instance? GridLayout b))
        (is (= 1 (count c))))))

  )


