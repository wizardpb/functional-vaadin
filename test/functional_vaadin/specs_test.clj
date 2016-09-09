(ns functional-vaadin.specs-test
  (:require [clojure.spec :as s])
  (:use [clojure.test]
        [functional-vaadin.build-support])
  (:import (clojure.lang MapEntry)
           (com.vaadin.server FileResource)
           (java.io File)))

(deftest menu-item
  (testing "fn spec"
    (let [icon (FileResource. (File. "fname"))
          ispec1 (->MenuItemSpec "sub1" nil identity)
          ispec2 (->MenuItemSpec "sub2" nil identity)]
      (is (=
            (s/conform :functional_vaadin.specs/menu_item_args (list "item" identity))
            {:name "item" :children (MapEntry. :item_fn identity)}))
      (is (=
            (s/conform :functional_vaadin.specs/menu_item_args (list "item" icon identity))
            {:name "item" :icon_resource icon :children (MapEntry. :item_fn identity)}))
      (is (=
            (s/conform :functional_vaadin.specs/menu_item_args (list "item" ispec1 ispec2))
            {:name "item" :children (MapEntry. :sub_items [ispec1 ispec2])}))
      (is (=
            (s/conform :functional_vaadin.specs/menu_item_args (list "item"))
            ::s/invalid)))
    ))
