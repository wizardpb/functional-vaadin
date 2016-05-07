(ns functional-vaadin.ui.TestUI
  (:require [functional-vaadin.ui.test-ui-def :as u])
  (:gen-class :name ^{com.vaadin.annotations.Theme "valo"
                      com.vaadin.annotations.Push {}} functional_vaadin.ui.TestUI
              :extends com.vaadin.ui.UI
              :main false))

(defn -init
  [main-ui request]
  (u/define-test-ui main-ui)
  )

