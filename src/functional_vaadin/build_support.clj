(ns functional-vaadin.build-support
  "Functions useful in implementing all the builder functions in core"
  (:require [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.config :refer :all]
            [functional-vaadin.data-binding :refer :all]
            [functional-vaadin.utils :refer :all]
            [clojure.set :as set]
            [clojure.spec :as s])
  (:import (com.vaadin.ui
             Panel AbstractOrderedLayout GridLayout AbstractSplitPanel AbstractComponentContainer Table Alignment Table$Align FormLayout ComponentContainer MenuBar MenuBar$Command MenuBar$MenuItem Window Component)
           (java.util Map Collection)
           (java.lang.reflect Constructor)
           (clojure.lang Keyword)
           (com.vaadin.server Resource)
           ))

;; A MenuBar.Command that allows functions as Menu Items

(defrecord FunctionCommand [cmd-fn]
  MenuBar$Command
  (^void menuSelected [this ^MenuBar$MenuItem item] (cmd-fn item)))

;; An interface for defining MenuItems

(defprotocol IMenuItemSpec
  (hasChildren? [this])
  (addFrom [this mbi])
  (getChildren [this])
  )

(deftype MenuItemSpec [name resource content]
  IMenuItemSpec
  (hasChildren? [this] (instance? Collection content))
  (addFrom [this mbi]
    {:pre [(or (nil? content) (fn? content))]}
    (if (nil? content)
      (.addSeparator mbi)
      (.addItem mbi name resource (->FunctionCommand content))))
  (getChildren [this] content)
  )

;; Argument parsing

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

;; Widget creation

(defn- apply-parent-config
  "Apply any options save on the child under the key :parent-data."
  [config parent child]
  (if-let [config (detach-data child :parent-data)]
    ;; Use do-configure so the parent opst aren't re-extracted
    (do-configure parent config))
  )

(defn- match-arg
  "Return the argument if it's type matches ctor-type. Matching is done using Java assignability. Since Clojure uses
  integer Long types exclusively, Integer types match a Long argument, and it is converted to a raw int. Return nil
  if there is no match"
  [^Class ctor-type arg]
  (cond
    (and (= ctor-type Integer/TYPE) (= (class arg) Long)) (int arg)
    (.isAssignableFrom ctor-type (class arg)) arg
    :else nil))

(defn- match-args
  " Match possible (non-empty) arguments with ctor argument types. Return matched (possibly converted) arguments"
  [ctor-param-types args]
  (if (and (> (count args) 0) (= (count ctor-param-types) (count args)))
    (let [conv-args (map match-arg ctor-param-types args)] (if (every? identity conv-args) conv-args)))
  )

(defn- null-ctor-for [cls]
  {:ctor (.getConstructor cls (make-array Class 0)) :ctor-args '()})

(defn- buildable-childen?
  "Can all these args be considered vald children of a widget? True if they are all Components or all MenuItemSpecs"
  [args]
  (or
    (every? #(instance? Component %) args)
    (every? #(instance? MenuItemSpec %) args)))

(defn- find-constructor
  "Find a constructor for the given class and arguments, matching argument types against the arguments
  Longer matches take precendent, and assignability determines an argument match.

  If no arguments are passed, just return the no-arg constructor.

  If no constructor is found, but all arguments are valid children, return the no-arg constructor so they get interpreted as children
  by the widget creator"

  [cls args]
  (letfn [(match-ctor [ctor]
            (if-let [conv-args (match-args (seq (.getParameterTypes ctor)) (take (.getParameterCount ctor) args))]
              {:ctor ctor :ctor-args conv-args}))]
    (if (zero? (count args))
      (null-ctor-for cls)
      (let [ctor-list (sort #(>= (.getParameterCount %1) (.getParameterCount %2)) (seq (.getConstructors cls)))]
        (or
          (some match-ctor ctor-list)
          (if (buildable-childen? args) (null-ctor-for cls) nil))))))

(defn- parse-builder-args
  "Parse builder arguments, returning a constructor, it's arguments, any unused constructor arguments,
  an optional config map, and any children"
  [cls args]
  (let [split-args (s/conform ::component-args args)
        cls-name (.getSimpleName cls)]
    (if (= split-args ::s/invalid)
      (bad-argument "Bad format building " cls-name ": " (s/explain-str ::component-args args))
      (let [{:keys [initial-args config]} split-args
            {:keys [ctor-args] :as ctor-map} (find-constructor cls initial-args)]
        ; If there is no config, the initial-args are a combination of children and ctor args, so split out any children
        ; otherwise just set any initial arguments that haven't been matched to the constructor
        (merge split-args ctor-map (if config
                                     {:unused-args (drop (count ctor-args) initial-args)}
                                     {:unused-args [] :children (drop (count ctor-args) initial-args)})
          )

        ))
    ))

(defn- do-create-widget
  "Create Vaadin widgets from a list of arguments. The variable arguments are a list of constructor values, an optional config Map
  and any child widgets.
  "
  [cls args allow-children]
  (let [{:keys [ctor ctor-args initial-args unused-args config children]} (parse-builder-args cls args)
        cls-name (.getSimpleName cls)]
    (cond
      (not ctor) (bad-argument "Cannot create a " cls-name " from " initial-args)
      (and (not allow-children) (not-empty children)) (bad-argument cls-name " does not allow children components")
      (not-empty unused-args) (bad-argument "Unknown extra arguments after constructor args "
                                (drop (count ctor-args) initial-args)))

    (let [obj (.newInstance ctor (object-array ctor-args))]
      [(if config (configure obj config) obj) (or children '())])
    ))

;(let [null-ctor (.getConstructor cls (make-array Class 0))]
;
;  (letfn [
;          (unwrap-children [obj computed-children?]
;            (let [children (unwrap-sequence-args computed-children?)]
;              (if (and (not allow-children) (> (count children) 0))
;               (bad-argument (.getSimpleName cls) " does not allow children")
;               [obj children])))
;
;          (make-result [obj children]
;            (let [config (first children)]
;              (if (instance? Map config)         ; Configure if needed and check/unwrap children
;               (unwrap-children (configure obj config) (drop 1 children))
;               (unwrap-children obj children))))
;          ]
;
;    ;; First try for configuration only
;    (if (and null-ctor (instance? Map (first args)))
;      (make-result (.newInstance null-ctor (object-array 0)) args)
;
;      ;; Otherwise try and find a non-null constructor and use that
;      (if-let [[^Constructor ctor conv-args] (find-constructor cls args)]
;        (make-result
;          (.newInstance ctor (object-array (take (.getParameterCount ctor) conv-args)))
;          (drop (.getParameterCount ctor) args))
;
;        ;; Otherwise use the null constructor if any children satisfy allow-children
;        (if (and null-ctor (or allow-children (and (not allow-children) (zero? (count args)))))
;          (make-result (.newInstance null-ctor (object-array 0)) args)
;
;          ;; Otherwise, we fail
;          (bad-argument "Cannot create a " (.getSimpleName cls) " from " (unwrap-sequence-args args)))))))
;)

(defn create-widget
  ([cls args allow-children] (do-create-widget cls args allow-children))
  ([cls args]
   (let [[w c] (do-create-widget cls args false)]
     w)))

(defn create-form-content [args]
  (let [[arg1 arg2] args]
    (cond
      ;; Use the config to determine content. If it's there, remove it and configure the value with the rest of the config
      (and (instance? Map arg1) (:content arg1)) [
                                                  (configure (:content arg1) (dissoc arg1 :content))
                                                  (drop 1 args)]
      ; We have a content directly - configure it if the second argumen is a Map, otherwise that's the layout result
      (instance? ComponentContainer arg1) (if (instance? Map arg2)
                                            [(configure arg1 arg2) (drop 2 args)]
                                            [arg1 (drop 1 args)])
      ; Otherwise defaul content is a normally-created FormLayout
      :else (create-widget FormLayout args true))))

;; Adding content

(defmulti add-children (fn [parent children] (class parent)))

(defmethod add-children :default [parent children]
  (unsupported-op "add-children undefined!!!" (class parent)))

(defn- set-children-as-content [obj children]
  (if (< 1 (count children))
    (bad-argument "You must set the content of a " (.getSimpleName (class obj)) " before adding multiple children, or provide a single child as content")
    (.setContent obj (if children (first children)))))

(defmethod add-children Panel [panel children]
  (if-let [content (.getContent panel)]
    (when-not (zero? (count children))
      (add-children content children))
    (set-children-as-content panel children))
  panel)

(defmethod add-children Window [window children]
  (if-let [content (.getContent window)]
    (add-children content children)
    (set-children-as-content window children))
  window)

(defmethod add-children AbstractComponentContainer [parent children]
  (doseq [child children]
    (.addComponent parent child))
  parent)

(defmethod add-children AbstractOrderedLayout [parent children]
  (doseq [child children]
    (.addComponent parent child)
    (do-configure parent (get-data child :parent-data)))
  parent)

(defmethod add-children GridLayout [^GridLayout parent children]
  (doseq [child children]
    ; Extract the grid layout child options
    (let [[grid-config parent-config] (extract-keys (get-data child :parent-data) #{:position :span})
          {[x y] :position [dx dy] :span} grid-config]
      (condp = (set (keys grid-config))
        #{} (.addComponent parent child)
        #{:position} (.addComponent parent child x y)
        #{:position :span} (.addComponent parent child x y (+ x dx -1) (+ y dy -1))
        #{:span} (bad-argument ":span requires a :position value as well"))
      (do-configure parent parent-config)))
  parent)

(defmethod add-children AbstractSplitPanel [parent children]
  (doseq [child children]
    (.addComponent parent child))
  parent)



(defn ->MenItemSeparator []
  (->MenuItemSpec nil nil nil))

(defn add-menu-item [mbi item]
  (let [sub-item (addFrom item mbi)]
    (if (hasChildren? item)
      (loop [children (getChildren item)]
        (when-not (empty? children)
          (add-menu-item sub-item (first children))
          (recur (rest children)))
        ))))

(defmethod add-children MenuBar [mb mitems]
  {:pre [(every? #(instance? MenuItemSpec %1) mitems)]}
  (doseq [mitem mitems]
    (add-menu-item mb mitem)))

;Tables

(defn translate-column-options [propertyId column-config]
  ; translate option :XXX to :columnXXX. This gets further translated to :setColumnXXX by (configure)
  ; Also add in the propertyId as the first argument
  (reduce (fn [tconfig [opt-key opt-arg]]
            (assoc tconfig
              (keyword (str "column" (capitalize (name opt-key))))
              [propertyId opt-arg]))
    {}
    column-config))

(defprotocol ITableColumn
  (addToTable [this table]))

(deftype TableColumn [options]
  ITableColumn
  (addToTable [this table]
    (let [[{:keys [propertyId type defaultValue]} column-config] (extract-keys options #{:propertyId :type :defaultValue})]
      (.addContainerProperty table propertyId type defaultValue)
      (->> column-config
        (translate-column-options propertyId)
        (configure table)))))

(defmethod add-children Table [table children]
  ; Children are configuration Maps of Table setters for that column
  (doseq [child children]
    (addToTable child table))
  table)
