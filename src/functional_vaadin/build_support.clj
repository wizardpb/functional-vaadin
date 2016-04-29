(ns functional-vaadin.build-support
  "Functions useful in implementing all the builder functions in core"
  (:require [functional-vaadin.config :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.ui
             Panel AbstractOrderedLayout GridLayout AbstractSplitPanel AbstractComponentContainer)
           (java.util Map)))


(defn- apply-parent-config
  "Apply any options save on the child under the key :parent-config."
  [parent child]
  (if-let [config (detach-data child :parent-config)]
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
        null-ctor (.getConstructor cls (make-array Class 0))
        error-msg (str "Cannot create a " (.getSimpleName cls) " from " args)]

    (letfn [(make-result [obj children]
              (if (and (not allow-children) (> (count children) 0))
                (throw (IllegalArgumentException. (str (.getSimpleName cls) " does not allow children")))
                [obj children]))]

      ;; First try for configuration
      (if (and null-ctor (instance? Map first-arg))
        (make-result
          (configure (.newInstance null-ctor (object-array 0)) first-arg)
          (rest args))

        ;; Otherwise try and find a non-null constructor and use that
       (if-let [[ctor conv-args] (find-constructor cls args)]
         (make-result
           (.newInstance ctor (object-array (take (.getParameterCount ctor) conv-args)))
           (drop (.getParameterCount ctor) args))

         ;; Otherwise use the null constructor if any children satisfy allow-children
         (if (and null-ctor (or allow-children (and (not allow-children) (zero? (count args)))))
           (make-result (.newInstance null-ctor (object-array 0)) args)

           ;; Otherwise, we fail
           (throw (IllegalArgumentException. error-msg))))))))

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

(defmethod add-children AbstractSplitPanel [parent children]
  (doseq [child children]
    (.addComponent parent child))
  parent)