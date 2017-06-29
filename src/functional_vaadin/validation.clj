;;
;; Copyright 2016 Prajna Inc. All Rights Reserved.
;;
;; This work is licensed under the Eclipse Public License v1.0 - https://www.eclipse.org/legal/epl-v10.html
;; Distrubition and use must be done under the terms of this license
;;

(ns functional-vaadin.validation
  "Interface to Vaadin validators and definition of a configurable, function-base validator"
  (:require [clojure.string :as str]
            [functional-vaadin.utils :refer :all])
  (:import (com.vaadin.data Validator Validator$InvalidValueException)))

(deftype FunctionalValidator [v-fn error-message]
  Validator
  (validate [this obj]
    (when-not (v-fn obj)
      (throw
        (Validator$InvalidValueException. (str/replace error-message "{0}" (str obj))))))
  )

