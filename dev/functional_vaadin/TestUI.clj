(ns functional-vaadin.TestUI
  (:require [functional-vaadin.core :refer :all]
            [functional-vaadin.builders :refer :all]
            [user :as u])
  (:gen-class :extends com.vaadin.ui.UI :main false))

(defn -init
  [main-ui request]
  (u/define-test-ui main-ui)
  )

