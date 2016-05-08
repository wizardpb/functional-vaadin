(ns functional-vaadin.build-support
  "Functions useful in implementing all the builder functions in core"
  (:require [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.config :refer :all]
            [functional-vaadin.conversion :refer :all]
            [functional-vaadin.utils :refer :all]
            [clojure.set :as set])
  (:import (com.vaadin.ui
             Panel AbstractOrderedLayout GridLayout AbstractSplitPanel AbstractComponentContainer Table Alignment Table$Align FormLayout)
           (java.util Map)
           (java.lang.reflect Constructor)
           (clojure.lang Keyword)))


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
                (throw (IllegalArgumentException. (str (.getSimpleName cls) " does not allow children")))
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
           (throw (IllegalArgumentException. (str "Cannot create a " (.getSimpleName cls) " from " args)))))))))

(defn create-form-content [args]
  (if (instance? Map (first args))
    ;; Use the config to determine content if it's there ...
    (let [[config & rest] args
          [form-config widget-config] (extract-keys config #{:content})]

     ; Create the layout (form) component  of the correct type
     (create-widget
       (or (:content form-config) FormLayout)
       (concat (list widget-config) rest) true))
    ; Otherwise defaul content is a FormLayout
    (create-widget FormLayout args true)))

;; Adding content

(defmulti add-children (fn [parent children] (class parent)))

(defmethod add-children :default [parent children]
  (throw (UnsupportedOperationException. (str "add-children udefined!!!" (class parent)))))

(defmethod add-children Panel [panel children]
  (if-let [content (.getContent panel)]
    (add-children content children)
    (if (> (count children) 0)
      (throw (IllegalArgumentException.
               "You must set the content of a Panel before adding children"))))
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

