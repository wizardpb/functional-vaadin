(ns functional-vaadin.test-ui
  (:use [functional-vaadin.core]
        [functional-vaadin.data-map]
        [functional-vaadin.utils]))

(defprotocol IComponentData
  (setData [this data])
  (getData [this]))

(deftype UIDataProvider [^:volatile-mutable data]
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
    (get-data this (concat [:components] (parse-key id))))

  (set-data-source [this component source-id]
    (let [key (concat [:data-map] (parse-key source-id))]
      (attach-data this key (conj (or (get-data this key) []) component))))

  (set-data-at [this source-id data]
    (doseq [component (get-data this (concat [:data-map] (parse-key source-id)))]
      (set-component-data component data)))

  (get-data-at [this component-id]
    (get-component-data (component-at this component-id)))
  )

