(ns functional-vaadin.examples.core
  (:require [functional-vaadin.core :refer :all]
            [functional-vaadin.rx.observers :as obs]
            [functional-vaadin.rx.operators :as ops])
  (:import (com.vaadin.ui Alignment CheckBox Table$Align Label Table)
           (com.vaadin.shared.ui.label ContentMode)
           (com.vaadin.ui.themes ValoTheme)
           (com.vaadin.server FontAwesome)
           (com.vaadin.data Container$Filter)
           (com.vaadin.data.util IndexedContainer))
  )

(deftype ToDoFilter [cmp-fn]
  Container$Filter
  (passesFilter [this itemId item]
    (cmp-fn (.getValue (.getValue (.getItemProperty item "check-box")))))
  (appliesToProperty [this propertyId] (= propertyId "check-box"))
  )

(defn ->CompletedFilter "Return a filter that shows only completed items" []
  (->ToDoFilter (fn [v] v)))

(defn ->PendingFilter "Return a filter that shows only pending items" []
  (->ToDoFilter (fn [v] (not v))))

(defn data-row-for [item]
  (let [cb (check-box)
        lbl (label item ContentMode/HTML)]
    (->> (obs/value-changes cb)
      (ops/consume-for lbl
        (fn [l {:keys [source event]}]
          (.setValue l (if (.getValue source) (str "<s style=\"color: #a3a3a3\">" item "</s>") item)))))
    (object-array [cb lbl])))

(defn add-todo-item [table {:keys [action sender target]}]
  (let [ui (.getUI table)
        page-length (inc (.getPageLength table))]
    (.setPageLength table page-length)
    (.addItem table (data-row-for (.getValue target)) nil)
    (.clear target)
    (.setValue (componentNamed :item-count ui) (str page-length " item" (if (> page-length 1) "s")))
    (.setVisible (componentNamed :table-panel ui) (not (zero? (.getPageLength table))))))

(defn remove-completed [table]
  (let [^IndexedContainer data-source (.getContainerDataSource table)
        filters (.getContainerFilters data-source)]
    (.removeAllContainerFilters data-source)
    (doseq [itemId (.getItemIds data-source)]
      (if (.getValue (.getValue (.getContainerProperty data-source itemId "check-box")))
        (.removeItem data-source itemId)))
    (doseq [f filters] (.addContainerFilter data-source f)))
  )

(defn animate [ui]
  ;; Enter adds a todo item
  (->>
    (obs/with-actions (componentNamed :text-panel ui) [{:name "Enter" :keycode 13}])
    (ops/consume-for (componentNamed :todo-list ui) add-todo-item))

  ;; Selection button observers for all three selction buttons
  (let [table (componentNamed :todo-list ui)]
    (->>
      (obs/button-clicks (componentNamed :button-all ui))
      (ops/consume-for table
        (fn [t {:keys [source]}]
          (.removeAllContainerFilters (.getContainerDataSource t))
          (.setEnabled source false)
          (doseq [b [:button-complete :button-todo]] (.setEnabled (componentNamed b ui) true))
          )))
    (->>
      (obs/button-clicks (componentNamed :button-complete ui))
      (ops/consume-for table
        (fn [t {:keys [source]}]
          (.removeAllContainerFilters (.getContainerDataSource t))
          (.addContainerFilter (.getContainerDataSource t) (->CompletedFilter))
          (.setEnabled source false)
          (doseq [b [:button-all :button-todo]] (.setEnabled (componentNamed b ui) true))
          )))
    (->>
      (obs/button-clicks (componentNamed :button-todo ui))
      (ops/consume-for table
        (fn [t {:keys [source]}]
          (.removeAllContainerFilters (.getContainerDataSource t))
          (.addContainerFilter (.getContainerDataSource t) (->PendingFilter))
          (.setEnabled source false)
          (doseq [b [:button-complete :button-all]] (.setEnabled (componentNamed b ui) true))
          )))

    ; Clear completed items
    (->>
      (obs/button-clicks (componentNamed :button-clear ui))
      (ops/consume-for table
        (fn [t {:keys [source]}] (remove-completed t))))

    ))

(defn button-panel []
  (horizontal-layout {:width "99%" :height "50px" :alignment Alignment/MIDDLE_CENTER}
    (label "" {:id :item-count :alignment Alignment/MIDDLE_LEFT})
    (horizontal-layout {:spacing true :alignment Alignment/MIDDLE_CENTER}
      (button "All" {:id :button-all :styleName ValoTheme/BUTTON_TINY :enabled false})
      (button "Completed" {:id :button-complete :styleName ValoTheme/BUTTON_TINY})
      (button "To Do" {:id :button-todo :styleName ValoTheme/BUTTON_TINY })
      )
    (button "Clear completed" {:id :button-clear
                               :styleName ValoTheme/BUTTON_TINY :alignment Alignment/MIDDLE_RIGHT
                               })))

(defn todo-ui-spec [ui]
  (defui ui
    (vertical-layout {:width "100%" :margin [:top]}
      (label {:value "ToDo" :alignment Alignment/MIDDLE_CENTER :widthUndefined [] :height "70px"})
      (panel {:id :text-panel :alignment Alignment/MIDDLE_CENTER :width "50%"}
        (text-field {:id :entry-field
                     :immediate true :sizeFull []
                     :inputPrompt "Enter something to do"}))
      (vertical-layout {:id :table-panel :heightUndefined [] :width "50%"
                        ;:margin [:left :right]
                        :visible false
                        :alignment Alignment/MIDDLE_CENTER}
        (table {:id :todo-list
               :alignment Alignment/MIDDLE_CENTER :width "99%" :pageLength 0}
         (table-column "check-box" {:width 60 :header "" :icon FontAwesome/CHEVRON_DOWN :type CheckBox :alignment Table$Align/CENTER})
         (table-column "list-item" {:type Label :header ""}))
        (button-panel)
        )))
  (animate ui)
  )