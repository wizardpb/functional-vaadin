(ns functional-vaadin.build-support
  "Functions useful in implementing all the builder functions in core"
  (:require [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.config :refer :all]
            [functional-vaadin.conversion :refer :all]
            [functional-vaadin.utils :refer :all]
            [clojure.set :as set])
  (:import (com.vaadin.ui
             Panel AbstractOrderedLayout GridLayout AbstractSplitPanel AbstractComponentContainer Table Alignment Table$Align FormLayout ComponentContainer MenuBar MenuBar$Command MenuBar$MenuItem)
           (java.util Map Collection)
           (java.lang.reflect Constructor)
           (clojure.lang Keyword)
           (com.vaadin.server Resource)))


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
              true nil))
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

(defn create-widget
  "Create Vaadin widgets using, if possible, the initial n items of args as constructor argument.

  For n > 0, try and match the types of a constructors parameters againt the first n arguments types. For multiple
  matches, the longest constructor arglist is preferred. If the first argument is a Map,
  and there is a null constructor, prefer that to any constructors, and construct and congfigure the widget from the
  map. If no constructors match, and there is a null constructor, create a widget using that

  Any remaining arguments are treated as children, unless allow-children is false. The new object and children are
  returned in a vector. If there is no match to the arguments are made, and exception is thrown"
  [cls args allow-children]
  (let [first-arg (first args)
        null-ctor (.getConstructor cls (make-array Class 0))]

    (letfn [(make-result [obj children]
              (if (and (not allow-children) (> (count children) 0))
                (bad-argument (.getSimpleName cls) " does not allow children")
                [obj children]))
            ]

      ;; First try for configuration
      (if (and null-ctor (instance? Map first-arg))
        (make-result
          (configure (.newInstance null-ctor (object-array 0)) first-arg)
          (rest args))

        ;; Otherwise try and find a non-null constructor and use that
        (if-let [[^Constructor ctor conv-args] (find-constructor cls args)]
          (make-result
            (.newInstance ctor (object-array (take (.getParameterCount ctor) conv-args)))
            (drop (.getParameterCount ctor) args))

          ;; Otherwise use the null constructor if any children satisfy allow-children
          (if (and null-ctor (or allow-children (and (not allow-children) (zero? (count args)))))
            (make-result (.newInstance null-ctor (object-array 0)) args)

            ;; Otherwise, we fail
            (bad-argument "Cannot create a " (.getSimpleName cls) " from " args)))))))

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
      true (create-widget FormLayout args true))))

;; Adding content

(defmulti add-children (fn [parent children] (class parent)))

(defmethod add-children :default [parent children]
  (unsupported-op "add-children udefined!!!" (class parent)))

(defmethod add-children Panel [panel children]
  (if-let [content (.getContent panel)]
    (add-children content children)
    (if (> (count children) 0)
      (bad-argument "You must set the content of a Panel before adding children")))
  panel)

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

(defn parse-menu-item
  "Determine the menu item type (cmd or sub-menu) by looking at the arguments passed"
  [name args]
  (letfn [(valid-fn [it] (fn? it))
          (valid-resource [it] (instance? Resource it))
          (valid-child [it] (instance? MenuItemSpec it))]
    (cond
      (zero? (count args)) (bad-argument "You must provide at least a function to a MenuItem: " name)
      ; Just a functionn command
      (= 1 (count args)) (let [[first] args]
                           (cond
                             (valid-fn first) (->MenuItemSpec name nil first )
                             (valid-child first) (->MenuItemSpec name nil args)
                             true (bad-argument "Argument for " name " is not a function: " first)))
      ; Command or sub-menu with icon...
      (= (count args) 2) (let [[first second] args]
                           (cond
                             (and (valid-resource first) (valid-fn second)) (->MenuItemSpec name first second)
                             (and (valid-resource first) (valid-child second)) (->MenuItemSpec name first (list second))
                             (every? valid-child args) (->MenuItemSpec name nil args)
                             true (bad-argument "Incorrect arguments for " name ": " args)
                             ))
      (> (count args) 2) (let [[first & children] args]
                           (cond
                             (and                       ;Resource and menu-items
                               (valid-resource first)
                               (every? valid-child children)) (->MenuItemSpec name first args)
                             (every? valid-child args) (->MenuItemSpec name nil args)
                             true (bad-argument "Incorrect arguments for " name ": " args)
                             )))))

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
  ; Also add in the propertyIs as the first argument
  (reduce (fn [tconfig [opt-key opt-arg]]
            (assoc tconfig
              (keyword (str "column" (capitalize (name opt-key))))
              [propertyId opt-arg]))
    {}
    column-config))

(defmethod add-children Table [table children]
  ; Children are configuration Maps of Table setters for that column
  (doseq [child children]
    (let [[{:keys [propertyId type defaultValue]} column-config] (extract-keys child #{:propertyId :type :defaultValue})]
      (.addContainerProperty table propertyId type defaultValue)
      (->> column-config
        (translate-column-options propertyId)
        (configure table))))
  table)

;; Field building

(defn- parse-form-field-args
  "Parse an argument list for a form field. Syntax is
  [property-id property-type? widget-config?]

  Returns [propery-id (or property-type Object) (or widget-config {})]"
  [args]
  (let [[prop-id prop-type widget-config] args]
    (cond
      (zero? (count args)) (bad-argument "You must supply at least a property id for a form field")
      (= 1 (count args)) (cond
                           (not (or (instance? String prop-id)
                                  (instance? Keyword prop-id))) (bad-argument "Property id must be a String or keyword")
                           true [prop-id Object {}])
      (= 2 (count args)) (cond
                           (and (not (instance? Class prop-type))
                             (not (instance? Map prop-type))) (bad-argument "Second argument shoud be type or config map")
                           true (if (instance? Class prop-type)
                                  [prop-id prop-type {}]
                                  [prop-id Object prop-type]));Second arg is the config
      (= 3 (count args)) (cond
                           (not (instance? Class prop-type)) (bad-argument "Property type must be a class")
                           (not (instance? Map widget-config)) (bad-argument "Invalid configuration")
                           true [prop-id prop-type widget-config]))
    )
  )

(defn create-field
  "Create a Field object, dealing with both Form and non-Form fields"
  [field-class args]
  (if *current-field-group*
    (let [[prop-id prop-type widget-config] (parse-form-field-args args)
          field (first (create-widget field-class (list widget-config) false))
          data-source (.getItemDataSource *current-field-group*)
          data-prop-ids (set (.getItemPropertyIds data-source))]
      (if (not (contains? data-prop-ids prop-id))
        (.addItemProperty data-source prop-id (->Property nil prop-type)))
      (.bind *current-field-group* field prop-id)
      (if (nil? (.getCaption field))
        (.setCaption field (humanize prop-id)))
      field
      )
    (first (create-widget field-class args false)))
  )

