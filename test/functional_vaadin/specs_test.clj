(ns functional-vaadin.specs-test
  (:require [clojure.spec :as s])
  (:use [clojure.test]
        [functional-vaadin.specs]
        [functional-vaadin.build-support])
  (:import (clojure.lang MapEntry)
           (com.vaadin.server FileResource)
           (java.io File)
           (com.vaadin.ui Button VerticalLayout)))

(deftest component-args
  (testing "conformance"
    (is (= (s/conform ::functional-vaadin.specs/component-args '())
          {}))
    (is (= (s/conform ::functional-vaadin.specs/component-args [{:hidden true}])
          {:config {:hidden true}}))
    (is (= (s/conform ::functional-vaadin.specs/component-args '("c1" "c2"))
          {:initial-args ["c1" "c2"]}))
    (is (= (s/conform ::functional-vaadin.specs/component-args '("c1" "c2" {:hidden true}))
          {:initial-args ["c1" "c2"] :config {:hidden true}}))
    (let [c1 (Button.)
          c2 (VerticalLayout.)]
      (is (= (s/conform ::functional-vaadin.specs/component-args (list {:hidden true} c1 c2))
            {:config {:hidden true} :children [c1 c2]}))
      (is (= (s/conform ::functional-vaadin.specs/component-args (list "c1" c1 {:hidden true} c1 c2))
            {:initial-args ["c1" c1] :config {:hidden true} :children [c1 c2]})))
    )
  (testing "failed conformance"
    (is (= (s/conform ::functional-vaadin.specs/component-args [{:hidden true} {:enabled false}])
          ::s/invalid))
    (let [c1 (Button.)
          c2 (VerticalLayout.)]
      (is (= (s/conform ::functional-vaadin.specs/component-args ["c1" {:hidden true} {:enabled false} c1])
            ::s/invalid))))
  )

(deftest menu-item
  (testing "fn spec"
    (let [icon (FileResource. (File. "fname"))
          ispec1 (->MenuItemSpec "sub1" nil identity)
          ispec2 (->MenuItemSpec "sub2" nil identity)]
      (is (=
            (s/conform :functional-vaadin.specs/menu-item-args (list "item" identity))
            {:name "item" :children (MapEntry. :item_fn identity)}))
      (is (=
            (s/conform :functional-vaadin.specs/menu-item-args (list "item" icon identity))
            {:name "item" :icon_resource icon :children (MapEntry. :item_fn identity)}))
      (is (=
            (s/conform :functional-vaadin.specs/menu-item-args (list "item" ispec1 ispec2))
            {:name "item" :children (MapEntry. :sub_items [ispec1 ispec2])}))
      (is (=
            (s/conform :functional-vaadin.specs/menu-item-args (list "item"))
            ::s/invalid)))
    ))
