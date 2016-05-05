(ns functional-vaadin.naming
  (:require [functional-vaadin.utils :refer :all])
  (:import (clojure.lang Keyword)
           (com.vaadin.ui Component)))


(defn addComponent [ui ^Component component ^Keyword id]
              (let [ks (component-key id)]
                (if (get-data component ks)
                  (bad-argument (str "There is already a component named " id)))
                (attach-data ui ks component)))

(defn componentAt [ui ^Keyword id]
             (get-data ui (component-key id)))


