(ns functional_vaadin.specs
  (:require [clojure.spec :as s])
  (:use functional-vaadin.build-support)
  (:import (com.vaadin.server Resource)
           (functional_vaadin.build_support MenuItemSpec)))

(s/def ::menu_items (s/* #(instance? IMenuItemSpec %)))
(s/def ::menu_item_args
  (s/cat :name string?
    :icon_resource (s/? #(instance? Resource %))
    :children (s/alt :item_fn fn? :sub_items (s/+ #(instance? MenuItemSpec %)))))