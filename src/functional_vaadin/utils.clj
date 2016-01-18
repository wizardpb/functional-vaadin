(ns functional-vaadin.utils
  (:require [clojure.string :as str])
  )

(defmacro set-property
  "Set the value of a Java property defined by a method set<name>"
  [jobj name value]
  `(~(symbol (str ".set" (str/capitalize name))) ~jobj ~value))