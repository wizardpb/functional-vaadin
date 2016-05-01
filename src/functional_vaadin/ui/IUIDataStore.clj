(ns functional-vaadin.ui.IUIDataStore)

(gen-interface
  :name functional_vaadin.ui.IUIDataStore
  :methods [
            [addComponent [com.vaadin.ui.Component clojure.lang.Keyword] com.vaadin.ui.Component]
            [componentAt [clojure.lang.Keyword] com.vaadin.ui.Component]
            [bind [clojure.lang.Keyword clojure.lang.Keyword] void]
            [updateBinding [clojure.lang.Keyword clojure.lang.IFn] Object]
            [getBindingValue [clojure.lang.Keyword] Object]
            ]
  )
