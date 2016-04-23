(ns config-gen
  (:require [clojure.string :as str])
  (:use [clojure.set]
        [clojure.java.io]
        [clojure.test])
  (:import (com.vaadin.ui
             Label Embedded Link MenuBar Upload Button Calendar GridLayout
             TabSheet VerticalSplitPanel HorizontalSplitPanel Slider TextField TextArea PasswordField CheckBox
             RichTextArea InlineDateField PopupDateField Table ComboBox TwinColSelect NativeSelect
             ListSelect OptionGroup Tree TreeTable Panel VerticalLayout HorizontalLayout FormLayout)
           ))

(def configurable-classes
  [
   Label
   Embedded
   Link
   MenuBar
   Upload
   Button
   Calendar
   GridLayout
   Panel
   VerticalLayout
   HorizontalLayout
   FormLayout
   TabSheet
   VerticalSplitPanel
   HorizontalSplitPanel
   Slider
   TextField
   TextArea
   PasswordField
   ;;TODO - ProgressBar - needs threading support, maybe hide/unhide on start ?
   CheckBox
   RichTextArea
   InlineDateField
   PopupDateField
   Table
   ComboBox
   TwinColSelect
   NativeSelect
   ListSelect
   OptionGroup
   Tree
   TreeTable
   ;; TODO - Converter - how to add to session
   ])

(def preample "(ns functional-vaadin.config-table
  (:use [functional-vaadin.config-funcs])
  (:import (com.vaadin.ui
            Component AbstractComponent Label Embedded Link MenuBar CustomComponent Upload Button Calendar GridLayout
            TabSheet VerticalSplitPanel HorizontalSplitPanel Slider TextField TextArea PasswordField CheckBox
            RichTextArea InlineDateField PopupDateField Table ComboBox TwinColSelect NativeSelect
            ListSelect OptionGroup Tree TreeTable Panel VerticalLayout HorizontalLayout FormLayout)))

(def config-table
  {\n")

(def postamble "})")

(defn extract-setters
  "Extract all setters of the form 'setXXX' from the configurable classes list. Return a set of tuples
  {:name XXX :argcount N}. Acc is a transient set"
  [acc cls]
  (let [setters (filter #(= (subs (:name %1) 0 3) "set")
                        (map (fn [m] {:name (.getName m) :argcount (.getParameterCount m)})
                             (.getMethods cls)))]
    (doseq [s setters]
      (conj! acc s)))
  acc)


(defn gen-config-table []
  (let [opt-list
        (sort #(compare (:name %1) (:name %2))
              (persistent!
                (reduce extract-setters (transient #{}) configurable-classes)))]
    (with-open [f (writer "src/functional_vaadin/config_table.clj")]
      (.write f preample)
      (doseq [opt opt-list]
        (let [arg-count (:argcount opt)
              arg-string (str/join " "
                                   (map #(str "arg" %1) (range 0 arg-count)))
              opt-name (:name opt)]
          (.write f
                  (str "    [:" opt-name " " arg-count "] (fn [obj " arg-string "] (."
                       opt-name
                       " obj " arg-string "))\n"))))
      (.write f postamble))))

(defn all-setters [cls]
  (sort #(compare (first %1) (first %2)) (filter #(= (subs (first %1) 0 3) "set")
                                                 (map (fn [m] [(.getName m) (.getParameterCount m)])
                                                      (.getMethods cls)))))

(defn has-dup-setters? [cls]
  (let [setters (all-setters cls)]
    (not= (count setters) (count (set setters)))))

(defn find-dups [cls]
  (let [s (all-setters cls)]
    (letfn [(update-count
              [acc s]
              (update acc s #(if %1 (inc %1) 1)))]
      (filter #(> (last %1) 1) (reduce update-count {} (all-setters cls))))))