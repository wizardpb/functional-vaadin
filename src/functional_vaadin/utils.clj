(ns functional-vaadin.utils
  (:require [clojure.string :as str]))

(defn capitalize [s]
  (if (empty? s) s (str (.toUpperCase (subs s 0 1)) (subs s 1))))

(defn uncapitalize [s]
  (if (empty? s) s (str (.toLowerCase (subs s 0 1)) (subs s 1))))
