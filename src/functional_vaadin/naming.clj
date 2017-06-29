;;
;; Copyright 2016 Prajna Inc. All Rights Reserved.
;;
;; This work is licensed under the Eclipse Public License v1.0 - https://www.eclipse.org/legal/epl-v10.html
;; Distrubition and use must be done under the terms of this license
;;

(ns functional-vaadin.naming
  (:require [functional-vaadin.utils :refer :all])
  (:import (clojure.lang Keyword)
           (com.vaadin.ui Component)))

(defn addComponent [ui ^Component component ^Keyword id]
  (let [ks (component-key id)]
    (if (get-data component ks)
      (bad-argument "There is already a component named " id))
    (attach-data ui ks component)))

(defn componentAt [ui ^Keyword id]
  (get-data ui (component-key id)))


