(ns functional-vaadin.ui.TestUI
  (:require [functional-vaadin.core :refer :all]
            [user :as u])
  (:gen-class :extends functional_vaadin.ui.FunctionalUI
              :main false))

(defn -init
  [main-ui request]
  (u/define-test-ui main-ui)
  )

