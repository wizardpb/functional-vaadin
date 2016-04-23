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
  (if (not (instance? Map opts))
    (throw (IllegalArgumentException. "Configuration options must be a Map")))
  (doseq [opt-spec opts]
    (configure-component obj opt-spec)))

(defn configure [obj ^Map opts]
  (do-configure obj (extract-parent-options opts obj))
  obj)

