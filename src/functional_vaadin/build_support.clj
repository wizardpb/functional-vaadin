(ns functional-vaadin.build-support
  "Functions useful in implementing all the builder functions in core"
  (:require [functional-vaadin.config :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.ui Panel AbstractOrderedLayout GridLayout)))


(defn- apply-parent-config
  "Apply any options save on the child under the key :parent-config."
  [parent child]
  (if-let [config (detach-data child :parent-config)]
    ;; Use do-configure so the parent opst aren't re-extracted
    (do-configure parent config))
  )

(defmulti add-children (fn [parent children] (class parent)))

(defmethod add-children :default [parent children]
  (throw (UnsupportedOperationException. (str "Cannot add children to an instance of " (class parent)))))

(defmethod add-children Panel [panel children]
  (let [content (.getContent panel)]
    (when content (add-children content children))
    panel))

(defmethod add-children AbstractOrderedLayout [parent children]
  (doseq [child children]
    (.addComponent parent child)
    (apply-parent-config parent child))
  parent)

(defmethod add-children GridLayout [^GridLayout parent children]
  (doseq [child children]
    (let [pdata (get-data child :parent-data)
          {[x y] :position [dx dy] :span} pdata]
      (condp = (set (keys pdata))
        #{} (.addComponent parent child)
        #{:position} (.addComponent parent child x y)
        #{:position :span} (.addComponent parent child x y (+ x dx -1) (+ y dy -1)))
      (apply-parent-config parent child)))
  parent)