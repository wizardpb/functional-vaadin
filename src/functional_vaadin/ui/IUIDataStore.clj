(ns functional-vaadin.ui.IUIDataStore
  (:import [com.vaadin.ui Component]
           [clojure.lang IFn]))

(gen-interface
  :name functional_vaadin.ui.IUIDataStore
  :methods [
            [addComponent [com.vaadin.ui.Component Object] com.vaadin.ui.Component]
            [componentAt [Object] com.vaadin.ui.Component]
            [bind [Object Object] void]
            [setBinding [Object Object] Object]
            [getBinding [Object] Object]
            ]
  )
