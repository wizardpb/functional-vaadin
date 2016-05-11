(ns functional-vaadin.validation
  "Interafce to Vaadin validators and definition of a cnfigurable, function-base validator"
  (:require [clojure.string :as str]
            [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.data Validator Validator$InvalidValueException)))

(deftype FunctionalValidator [v-fn error-message]
  Validator
  (validate [this obj] (when-not (v-fn obj)
                         (throw (Validator$InvalidValueException.
                                  (str/replace error-message "{0}" (str obj))))))
  )

