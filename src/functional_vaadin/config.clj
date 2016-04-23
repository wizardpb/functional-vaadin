(ns functional-vaadin.config
  "Functions for doing map-based configuration of Vaadinwidgets. See config-table namespace"
  (:use [functional-vaadin.config-table]
        [functional-vaadin.utils])
  (:import (java.util Map)))

(def parent-options
  "The set of config options to save on a child for execution by a parent. Keys are the options,
  values are fns to transform the child option value for the parent"
  {
   :expandRatio (fn [child optval] [:expandRatio [child optval]])
   :componentAlignment (fn [child optval] [:componentAlignment [child optval]])
   :alignment (fn [child optval] [:componentAlignment [child optval]])
   })

(def parent-data-options
  "A set of config options that are saved fo use in any way by parent.These differ from parent-options in
  that the configuration mechanism only extracts and saves them"
  #{
    :position                                               ;xy position of an element in a Grid Layout
    :span                                                   ;xy span of an element in a Grid Layout
    })

(defn parent-transform [child [optkey optval]]
  ((get parent-options optkey) child optval))

(defn- extract-parent-options
  "Extract and save any config values for application to a parent"
  [opts child]
  (if-let [popts (not-empty (select-keys opts (keys parent-options)))]
    (do
      (attach-data child :parent-options (into {} (map parent-transform (repeat child) popts)))
      (apply dissoc opts (keys parent-options)))
    opts))

(defn- extract-parent-data
  "Extract and save any child options for use by a parent"
  [opts child]
  (if-let [popts (not-empty (select-keys opts parent-data-options))]
    (do
      (attach-data child :parent-data popts)
      (apply dissoc opts parent-data-options))
    opts))

(defn- configure-component
  [obj [opt args]]
  (let [arg-list (if (not (or (seq? args) (vector? args))) [args] args)
        opt-key (keyword (str "set" (capitalize (name opt))))
        f (get config-table [opt-key (count arg-list)])]
    (if f
      (do
        (apply f obj arg-list))
      (throw (UnsupportedOperationException. (str "No such option for " (class obj) ": " opt))))))

(defn do-configure [obj ^Map opts]
  (doseq [opt-spec opts]
    (configure-component obj opt-spec)))

(defn configure [obj ^Map opts]
  (if (not (instance? Map opts))
    (throw (IllegalArgumentException. "Configuration options must be a Map")))
  (do-configure
    obj
    (-> opts
        (extract-parent-options obj)
        (extract-parent-data obj)))
  obj)

