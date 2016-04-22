(ns functional-vaadin.config
  (:require [functional-vaadin.config-table :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (java.util Map)))

(defn- do-config
  [obj [opt args]]
  (let [arg-list (if (not (or (seq? args) (vector? args))) [args] args)
        opt-key (keyword (str "set" (capitalize (name opt))))
        f (get config-table [opt-key (count arg-list)])]
    (if f
      (do
        (apply f obj arg-list))
      (throw (IllegalArgumentException. (str "No such option: " opt " for args " args))))))

(defn configure [obj ^Map opts]
  (doseq [opt-spec opts]
    (do-config obj opt-spec))
  obj)

