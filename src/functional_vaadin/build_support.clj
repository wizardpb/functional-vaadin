(ns functional-vaadin.build-support
  "Functions useful in implementing all the builder functions in core"
  (:require [functional-vaadin.config :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.ui Panel AbstractOrderedLayout)))


(defn- apply-parent-options
  "Apply any options save on the child under the key :parent-options."
  [parent child]
  (if-let [opts (detach-data child :parent-options)]
    ;; Use do-configure so the parent opst aren't re-extracted
    (do-configure parent opts))
  )

(defmulti add-children (fn [parent children] (class parent)))

(defmethod add-children :default [parent children]
  (throw (UnsupportedOperationException. (str "Cannot add children to an instance of " (class parent)))))

(defmethod add-children Panel [panel children]
  (let [content (.getContent panel)]
    (if content
      (add-children content children))
    panel))

(defmethod add-children AbstractOrderedLayout [parent children]
  (doseq [child children]
    (.addComponent parent child)
    (apply-parent-options parent child))
  parent)