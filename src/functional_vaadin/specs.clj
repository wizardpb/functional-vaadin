(ns functional-vaadin.specs
  (:require [clojure.spec :as s])
  (:use functional-vaadin.build-support)
  (:import (com.vaadin.server Resource)
           (functional_vaadin.build_support MenuItemSpec)
           (java.util Map)
           (com.vaadin.ui Component)))

(s/def ::menu-items (s/* #(instance? IMenuItemSpec %)))
(s/def ::menu-item-args
  (s/cat :name string?
    :icon_resource (s/? #(instance? Resource %))
    :children (s/alt :item_fn fn? :sub_items (s/+ #(instance? MenuItemSpec %)))))

(s/def ::component-args
  (s/cat
    :initial-args (s/* #(not (instance? Map %)))
    :config (s/? #(instance? Map %))
    :children (s/* #(instance? Component %))))