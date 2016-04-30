(ns functional-vaadin.mock-data-provider
  (:use [functional-vaadin.core]
        [functional-vaadin.utils])
  (:import (com.vaadin.ui SingleComponentContainer)))

(defprotocol IComponentData
  (setData [this data])
  (getData [this]))

(deftype UIDataProvider [^:volatile-mutable data ^:volatile-mutable content]

  SingleComponentContainer
  (setContent [this c] (set! content c))
  (getContent [this] content)
  (getComponentCount [this] (if content 1 0))

  IComponentData
  (setData [this d] (set! data d))
  (getData [this] data)

  IUIData
  (add-component [this component id]
    (let [ks (concat [:components] (parse-key id))]
      (if (get-data component ks)
        (throw (IllegalArgumentException. (str "There is already a component named " id))))
      (attach-data this ks component)))

  (component-at [this id]
    (get-data this (concat [:components] (parse-key id)))))

