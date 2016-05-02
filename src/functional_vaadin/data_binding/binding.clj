(ns functional-vaadin.data-binding.binding
  (:require [functional-vaadin.data-binding.getting :refer :all]
            [functional-vaadin.data-binding.setting :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.data Property Property$Viewer Item$Viewer Container$Viewer Item Container)
           (com.vaadin.data.fieldgroup FieldGroup)
           (com.vaadin.ui Table)
           (com.vaadin.data.util ObjectProperty)))

(defn ->Binding [init]
  (merge
    {:components #{}                                        ; Bound components
    :binder     nil                                         ; The Vaadin data binding object holding the data: Property, Item, Container
    :bind-type  nil                                         ; The binding type - how to assign binder to the component
    :structure  nil                                         ; The structure of the bound data
     } init))

;; Structure types: how should data be allocated to a binding object
;;   :Any - any object, but must be convertible to String
;;   :Map - conforms to Map interface
;;   :CollectionAny - a collection of :Any objects (usually used for AbstractSelect)
;;   :CollectionMap - a collection of Maps - used for tables

;; Bind types: (determine how to attach data to a component
;;   :Property - se setPropertyDataSource. Data structure is :Any
;;   :Item - use setItemDataSource. Data must be a :Map
;;   :Container - use setContainerDataSource. Data is :ContainerAny or :ContainerMap

(defn allocate-structure
  "Determine what data structure is appropriate for a given component and a desired bind type.
  Only a single structure is allowed for a given combination of component type and bind type"
  [component bind-type]
  (cond
    (and (= :Property bind-type) (instance? Property$Viewer component)) :Any
    (and (= :Item bind-type)
         (or (instance? Item$Viewer component) (instance? FieldGroup component))) :Map
    (and (= :Container bind-type) (instance? Table component)) :CollectionMap
    (and (= :Container bind-type) (instance? Container$Viewer component)) :CollectionAny
    true (throw (IllegalArgumentException.
                  (str "Cannot bind a " (.getSimpleName (class component)) " as a " bind-type)))))

(defn check-structure [binding new-structure]
  (if (not= (:structure binding) new-structure)
    (throw (IllegalArgumentException.
             (str "Incompatible combination of component and bind type. Already bound as "
                  (:structure binding))))))

(defn default-bind-type [component]
  {:pre (not (nil? component))
   :post (not (nil? %))}
  (cond
    (instance? Property component) :Property
    (or (instance? Item$Viewer component)
        (instance? FieldGroup component)) :Item
    (instance? Container$Viewer component) :Container))

(defn bind-component [ui bind-type component location-id]
  {:pre (not (nil? component))}
  (let [bind-key (binding-key location-id)
        binding (or (get-data ui bind-key) (->Binding {:bind-type bind-type}))]
    (if (contains? (:components binding) component)
      (throw (IllegalArgumentException. "Component is already bound to " location-id))
      (attach-data ui bind-key
                   (-> binding
                       (assoc :structure (check-structure binding (allocate-structure component bind-type)))
                       (assoc :components (conj (:components binding) component))))
      )
    )
  )

(defn update-binding [binding update-fn]
  {:pre (not (nil? binding))}
  (let [old-value (get-bound-value binding)]
    [old-value (set-bound-value binding (update-fn old-value))]))

(defn get-binding-value [binding]
  {:pre (not (nil? binding))}
  (get-bound-value binding))

