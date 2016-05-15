(ns functional-vaadin.examples.core
  (:require [functional-vaadin.core :refer :all]
            [functional-vaadin.rx.observers :as obs]
            [functional-vaadin.rx.operators :as ops])
  (:import (com.vaadin.ui Alignment))
  )

(defn todo-ui-spec [ui]
  (defui ui
    (vertical-layout {:margin true :spacing true}
      (label {:value "ToDo" :alignment Alignment/MIDDLE_CENTER :widthUndefined []})
      (panel {:alignment  Alignment/MIDDLE_CENTER :width "50%"}
        (text-field {:id :entry-field
                     :immediate true :sizeFull []
                     :inputPrompt "Enter something to do"}))
      (table {:id :todo-list
              :alignment Alignment/MIDDLE_CENTER :width "70%" :pageLength 1 }
        (table-column "check-box" {:width 60 :header "Done?"})
        (table-column "list-item" {:header "Item"}))))
  (->> (obs/value-changes (componentNamed :entry-field ui) )
    (ops/consume-for (componentNamed :todo-list ui)
      (fn [table {:keys [:source :event]}]
        (println (.getText event)))))
  )