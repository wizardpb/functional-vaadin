(ns functional-vaadin.utils-test
  (:require
    [clojure.test :refer :all]
    [functional-vaadin.utils :as ut])
  (:import (java.awt Point)))

(deftest set-property
  (is (let [p (Point.)]
        (ut/set-property p "location" (java.awt.Point. 1 1))
        (and (= (.getX p) 1.0)
             (= (.getY p) 1.0)))))