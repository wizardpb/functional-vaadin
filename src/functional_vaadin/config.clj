(ns functional-vaadin.config
  "Functions for doing map-based configuration of Vaadinwidgets. See config-table namespace"
  (:use [functional-vaadin.config-table]
        [functional-vaadin.utils])
  (:require [clojure.string :as str]
            [clojure.set :as set])
  (:import (java.util Map)))

(def
  ^{:dynamic true}
  *current-ui*
  "A dynamic var that will hold the current ui during building")

(def parent-attribute-specs
  "The definition of all attribute specs to save on a child for execution by a parent. Keys are the options,
  values are fns to transform the child option value for the parent"
  {
   :expandRatio (fn [child optval] [:expandRatio [child optval]])
   :componentAlignment (fn [child optval] [:componentAlignment [child optval]])
   :alignment (fn [child optval] [:componentAlignment [child optval]])
   })

(def parent-data-specs
  "The definition of all attribute specs that are saved for use in any way by parent other than as attribute specs.
  These differ from parent-options in that the configuration mechanism only extracts and saves them.
  Keys are options to extract, values are validation functions and error messages"
  {
   :position {                                              ; GridLayout position
              :validate (fn [vals] (and (vector? vals) (= 2 (count vals)) (every? integer? vals)))
              :error-msg "Grid position must be a vector of two integers"
              }
   :span     {                                              ; ; GridLayout span
              :validate (fn [vals] (and (vector? vals) (= 2 (count vals)) (every? integer? vals)))
              :error-msg "Element span must be a vector of two integers"}
   })

(def special-attribute-specs
  "The defintion of any attributes that get special processing. These add to, augment or override other config
  specs (e.g setid). Override or augment is determined by the override key in the spec"
  {
   :id {:func (fn [c id] (.add-component *current-ui* c (str/split id #"\."))) :override false}
   }
  )

(defn transform-parent-attribute-spec
  "Transform a parent attribute spec into a form applicable to the parent"
  [child [optkey optval]]
  ((get parent-attribute-specs optkey) child optval))

(defn- extract-parent-attribute-specs
  "Extract and save any config values for application to a parent"
  [opts child]
  (if-let [popts (not-empty (select-keys opts (keys parent-attribute-specs)))]
    (do
      (attach-data child :parent-config (into {} (map transform-parent-attribute-spec (repeat child) popts)))
      (apply dissoc opts (keys parent-attribute-specs)))
    opts))

(defn- validate-parent-data-sepc [acc [opt val]]
  (let [valdateFn (get-in parent-data-specs [opt :validate])]
    (if (not (valdateFn val))
      (conj acc (get-in parent-data-specs [opt :error-msg]))
      acc)))

(defn- extract-parent-data-specs
  "Extract, validate and save any child options for use by a parent"
  [opts child]
  (if-let [popts (not-empty (select-keys opts (keys parent-data-specs)))]
    (do
      (if (= #{:span} (set/intersection (set (keys popts)) #{:position :span}))
        (throw (IllegalArgumentException. "span option requires a postion")))
      (if-let [errors (not-empty (reduce validate-parent-data-sepc [] popts))]
        (throw (IllegalArgumentException. (str/join ", " errors))))
      (attach-data child :parent-data popts)
      (apply dissoc opts (keys parent-data-specs)))
    opts))

(defn- do-special-attribute-specs
  [opts component]
  (apply dissoc opts
         (reduce
           (fn [ remove-keys [opt-key opt-val]]
             (let [spec (get special-attribute-specs opt-key)]
               ((:func spec) component opt-val)
               (if (:override spec) (conj remove-keys opt-key) remove-keys)))
           []
           (select-keys opts (keys special-attribute-specs))))
  )

(defn- configure-component
  [obj [attribute args]]
  (let [arg-list (if (not (or (seq? args) (vector? args))) [args] args)
        opt-key (keyword (str "set" (capitalize (name attribute))))
        f (get config-table [opt-key (count arg-list)])]
    (if f
      (do
        (apply f obj arg-list))
      (throw (UnsupportedOperationException. (str "No such option for " (class obj) ": " attribute))))))

(defn do-configure [obj ^Map config]
  (doseq [attr-spec config]
    (configure-component obj attr-spec))
  obj)

(defn configure [obj ^Map config]
  (if (not (instance? Map config))
    (throw (IllegalArgumentException. "Configuration options must be a Map")))
  (do-configure
    obj
    (-> config
        (do-special-attribute-specs obj)
        (extract-parent-attribute-specs obj)
        (extract-parent-data-specs obj)))
  obj)

