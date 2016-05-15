(ns functional-vaadin.examples.ToDo
  (:require [functional-vaadin.examples.core :refer :all])
  (:gen-class :name ^{com.vaadin.annotations.Theme "valo"} functional_vaadin.examples.ToDo
              :extends com.vaadin.ui.UI
              :main false)
  (:import (com.vaadin.ui UI Alignment)
           (com.vaadin.server Sizeable)))

(defn -init [this request]
  (todo-ui-spec this)
  )
