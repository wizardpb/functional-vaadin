(ns functional-vaadin.build-support
  "Functions useful in implementing all the builder functions in core"
  (:require [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.config :refer :all]
            [functional-vaadin.data-binding :refer :all]
            [functional-vaadin.utils :refer :all]
            [clojure.set :as set])
  (:import (com.vaadin.ui
             Panel AbstractOrderedLayout GridLayout AbstractSplitPanel AbstractComponentContainer Table Alignment Table$Align FormLayout ComponentContainer MenuBar MenuBar$Command MenuBar$MenuItem Window Component)
           (java.util Map Collection)
           (java.lang.reflect Constructor)
           (clojure.lang Keyword)
           (com.vaadin.server Resource)
           ))


;; Widget creation

(defn- apply-parent-config
  "Apply any options save on the child under the key :parent-data."
  [config parent child]
  (if-let [config (detach-data child :parent-data)]
    ;; Use do-configure so the parent opst aren't re-extracted
    (do-configure parent config))
  )

(defn find-constructor
  "Find a constructor for the given class and arguments, matching argument types against the arguments
  Longer matches take precendent, and assignability determines an argument match. The null constructor is ignored"
  [cls args]
  (letfn [(match-arg                                        ;Match an arg and convert - deal with Long->int
            [^Class ctor-type arg]
            (cond
              (and (= ctor-type Integer/TYPE) (= (class arg) Long)) (int arg)
              (.isAssignableFrom ctor-type (class arg)) arg
              :else nil))
          (match-args                                       ;Match arguments with ctor types. Return (possibly converted) arguments
            [ctor-param-types args]
            (if (and (> (count ctor-param-types) 0) (= (count ctor-param-types) (count args)))
              (let [conv-args (map match-arg ctor-param-types args)]
                (if (every? identity conv-args)
                  conv-args)))
            )
          (match-ctor [ctor]
            (if-let [conv-args (match-args (seq (.getParameterTypes ctor)) (take (.getParameterCount ctor) args))]
              [ctor conv-args]))]
    (let [ctor-list (sort #(>= (.getParameterCount %1) (.getParameterCount %2)) (seq (.getConstructors cls)))]
      (some match-ctor ctor-list))))

(defn- unwrap-sequence-args
  "When computing children, the result often ends up as some kind of list/vector of the children. create-widget
  expects it's args value to a list of the children, but in the computed case, args is a single element list that
  *contains* the list of children. This function detects this and unpacks it."
  [args]
  (if (and
        (collection? args)
        (= 1 (count args))
        (collection? (first args))
        (every? #(instance? Component %1) (first args))
        )
    (first args)
    args)
  )

(defn- do-create-widget
  "Create Vaadin widgets using, if possible, the initial n items of args as constructor argument.

  For n > 0, try and match the types of a constructors parameters againt the first n arguments types. For multiple
  matches, the longest constructor arglist is preferred. If the first argument is a Map,
  and there is a null constructor, prefer that to any constructors, and construct and congfigure the widget from the
  map. If no constructors match, and there is a null constructor, create a widget using that

  Any remaining arguments are treated as an optionanl configuration Map, folloed by any children, unless allow-children
  is false. The new object is configured from the optional configuration Map, and this and any children are
  returned in a vector. If there is no match to the arguments are made, and exception is thrown"

  [cls args allow-children]
  (let [null-ctor (.getConstructor cls (make-array Class 0))]

    (letfn [
            (unwrap-children [obj computed-children?]
              (let [children (unwrap-sequence-args computed-children?)]
                (if (and (not allow-children) (> (count children) 0))
                 (bad-argument (.getSimpleName cls) " does not allow children")
                 [obj children])))

            (make-result [obj children]
              (let [config (first children)]
                (if (instance? Map config)         ; Configure if needed and check/unwrap children
                 (unwrap-children (configure obj config) (drop 1 children))
                 (unwrap-children obj children))))
            ]

      ;; First try for configuration only
      (if (and null-ctor (instance? Map (first args)))
        (make-result (.newInstance null-ctor (object-array 0)) args)

        ;; Otherwise try and find a non-null constructor and use that
        (if-let [[^Constructor ctor conv-args] (find-constructor cls args)]
          (make-result
            (.newInstance ctor (object-array (take (.getParameterCount ctor) conv-args)))
            (drop (.getParameterCount ctor) args))

          ;; Otherwise use the null constructor if any children satisfy allow-children
          (if (and null-ctor (or allow-children (and (not allow-children) (zero? (count args)))))
            (make-result (.newInstance null-ctor (object-array 0)) args)

            ;; Otherwise, we fail
            (bad-argument "Cannot create a " (.getSimpleName cls) " from " (unwrap-sequence-args args))))))))

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

;; A Command that allows functions as Menu Items

(defrecord FunctionCommand [cmd-fn]
  MenuBar$Command
  (^void menuSelected [this ^MenuBar$MenuItem item] (cmd-fn item)))

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
