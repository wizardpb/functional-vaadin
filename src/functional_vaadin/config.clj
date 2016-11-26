(ns functional-vaadin.config
  "Functions for doing map-based configuration of Vaadinwidgets. See config-table namespace"
  (:require [clojure.string :as str]
            [clojure.spec :as s]
            [functional-vaadin.thread-vars :refer :all]
            [functional-vaadin.config-table :refer :all]
            [functional-vaadin.event-handling :refer :all]
            [functional-vaadin.naming :refer :all]
            [functional-vaadin.data-binding :refer :all]
            [functional-vaadin.utils :refer :all])
  (:import (java.util Map Collection)
           (clojure.lang Keyword)
           (com.vaadin.ui AbstractComponent AbstractOrderedLayout Alignment)
           (com.vaadin.shared.ui MarginInfo)
           (com.vaadin.event Action Action$Listener Action$Notifier Action$Handler Action$Container)
           (com.vaadin.data Validator)
           ))

(def attribute-translation
  "A mapping for alternative names for configuration attribute keys"
  {:alignment :componentAlignment})

(defn- save-for-layout-parent
  "Save an option for setting as an attribute on the parent. The saved value is an argument list for the attribute setter"
  [^AbstractComponent child opt-key opt-value]
  (.setData child (assoc-in (or (.getData child) {}) [:parent-data opt-key] [child opt-value])))

(defn- save-for-grid-parent
  "Save an option for setting as an attribute on the parent. The saved value is an argument list for the attribute setter"
  [^AbstractComponent child opt-key opt-value]
  (.setData child (assoc-in (or (.getData child) {}) [:parent-data opt-key] opt-value)))

(defn validate-margin [opt-val]
  (or (instance? Boolean opt-val)
    (and (instance? Collection opt-val)
      (<= (count opt-val) 4)
      (every? #{:left :right :top :bottom :vertical :horizontal} opt-val)
      )))

(defn ^MarginInfo ->MarginInfo [opt-val]
  (if (instance? Boolean opt-val)
    (MarginInfo. ^Boolean opt-val)
    (MarginInfo.
      (boolean (some #{:top :vertical} opt-val))
      (boolean (some #{:right :horizontal} opt-val))
      (boolean (some #{:bottom :vertical} opt-val))
      (boolean (some #{:left :horizontal} opt-val))
      )
    ))

(defn- validate-bind-args [propertyId type initialValue]
  (if (not-of-type propertyId [String Keyword])
    (bad-argument "Property Id '" propertyId "' must be a String or Keyword"))
  (if (not (class? type))
    (bad-argument "Type spec must be a Class"))
  (if (not (or (nil? initialValue) (.isAssignableFrom type (class initialValue))))
    (bad-argument "Specified initial value '" initialValue "' and type '" type "' are incompatible")))

(defn- do-bind [field prop-id prop-type initVal]
  {:pre [(not (nil? *current-field-group*))]}
  (validate-bind-args prop-id prop-type initVal)
  (let [data-source (.getItemDataSource *current-field-group*)
        data-prop-ids (set (.getItemPropertyIds data-source))]
    (if (not (contains? data-prop-ids prop-id))
      (.addItemProperty data-source prop-id (->Property initVal prop-type)))
    (.bind *current-field-group* field prop-id)
    (if (nil? (.getCaption field))
      (.setCaption field (humanize prop-id)))
    field))

(defn bind-field [field opt-val]
  (condp instance? opt-val
    String (do-bind field opt-val Object nil)
    Map (let [{:keys [propertyId type initialValue]} opt-val]
          (do-bind field propertyId (or type Object) initialValue))
    Collection (let [[propertyId type initialValue] opt-val]
                 (do-bind field propertyId (or type Object) initialValue))))

(defn add-validations
  "Add validators to the given fields. Validators are a single or sequence of Validator instances"
  [field arg]
  (let [validators (if (collection? arg) arg [arg])]
    (reduce (fn [f v] (.addValidator f v) f) field validators)))

(defn- validator? [v]
  (instance? Validator v))

(defn- check-validators [val]
  (if (collection? val)
    (every? validator? val)
    (validator? val)))

(defn- add-validations [field arg]
  (let [validators (if (iterable? arg) arg [arg])]
    (reduce (fn [f v] (.addValidator f v) f) field validators)))

(defn- validate-actions [val]
  (or (instance? Action$Handler val)
    (and (seq val) (every? #(instance? Action$Listener %) (seq val)))))

(defn- add-actions [obj _ val]
  ;; Assume actions have been validated
  (cond
    (and (instance? Action$Handler val) (instance? Action$Container obj)) (.addActionHandler obj val)
    (instance? Action$Notifier obj) (doseq [a val] (.addAction obj a))
    :else (bad-argument "A " (class obj) " does not support adding " (if (seq val) "individual actions" "ActionHandlersƒ"))
    )
  )

(def synthetic-option-specs
  ""
  {
   :expandRatio        {; OrderedLayout expansionRation
                        :validate  (fn [optval] (or (instance? Double optval) (instance? Float optval)))
                        :error-msg "Expansion ration must be a Float"
                        :execute   save-for-layout-parent
                        }
   :componentAlignment {;OrderedLayout componentAlignment
                        :validate  (fn [optval] (instance? Alignment optval))
                        :error-msg "Component alignment must be an Alignment value"
                        :execute   save-for-layout-parent
                        }
   :position           {; GridLayout position
                        :validate  (fn [vals] (and (vector? vals) (= 2 (count vals)) (every? integer? vals)))
                        :error-msg "Grid position must be a vector of two integers"
                        :execute   save-for-grid-parent
                        }
   :span               {; GridLayout span
                        :validate  (fn [vals] (and (vector? vals) (= 2 (count vals)) (every? integer? vals)))
                        :error-msg "Element span must be a vector of two integers"
                        :execute   save-for-grid-parent}
   :margin             {
                        :validate  validate-margin
                        :execute   (fn [^AbstractOrderedLayout obj opt-key opt-val]
                                     (.setMargin obj (->MarginInfo opt-val)))
                        :error-msg "Margin info must be true/false or a vector of the keywords [:left :right :top :bottom :vertical :horizontal]"}
   :id                 {
                        :validate  (fn [val] (or (instance? Keyword val) (instance? String val)))
                        :execute   (fn [obj opt-key id]
                                     (if *current-ui* (addComponent *current-ui* obj (keyword id)))
                                     (.setId obj (name id)))
                        :error-msg "Component ID must be a String or Keyword"}
   :bindTo             {
                        :validate  (fn [val] (some #(instance? % val) #{Keyword String Map Collection}))
                        :execute   (fn [obj opt-key opt-val] (bind-field obj opt-val))
                        :error-msg ":bindTo ID must be a String or Keyword"}
   :addStyleName       {
                        :validate  (fn [val] (instance? String val))
                        :execute   (fn [obj opt-key opt-val] (.addStyleName obj opt-val))
                        :error-msg "Style name must be a String"}
   :validateWith       {
                        :validate  check-validators
                        :execute   (fn [obj opt-key opt-val] (add-validations obj opt-val))
                        :error-msg "Arguments must all be validators"}
   :actions            {
                        :validate  validate-actions
                        :execute   add-actions
                        :error-msg "Arguments must be a non-empty list of Action.Listeners or an Action.Handler"
                        }

   })

(defn- validate-option [errors [opt-key opt-val] specs]
  (if-let [{v-fn :validate msg :error-msg} (get specs opt-key)]
    (if (not (v-fn opt-val))
      (conj errors msg))
    errors)
  )

(defn- validate-config [config specs]
  (reduce #(validate-option %1 %2 specs) [] config))

(defn- translate-config-keys [config]
  (reduce (fn [nopts [k v]] (assoc nopts (or (get attribute-translation k) k) v)) {} config))

(defn- do-synthetic-option [obj [opt-key opt-val]]
  ((get-in synthetic-option-specs [opt-key :execute]) obj opt-key opt-val))

;(defn do-synthetic-options [obj config]
;  (let [[syn-config attr-config] (extract-keys config (keys synthetic-option-specs))]
;    (doseq [option syn-config]
;      (do-syntheric-option obj option))
;    attr-config))

(defn- do-synthetic-options [obj config]
  (let [syn-keys (set (keys synthetic-option-specs))]
    (reduce (fn [config-opts option]
              (if (syn-keys (first option))
                (do (do-synthetic-option obj option) config-opts)
                (merge config-opts option))
             ) {} config)))

(defn- set-attribute
  [obj [attribute args]]
  (let [arg-list (if (nil? args)
                   []
                   (if (not (or (seq? args) (vector? args))) [args] args))
        attr-setter (keyword (str "set" (capitalize (name attribute))))
        f (get config-table [attr-setter (count arg-list)])]
    (if f
      (do
        (apply f obj arg-list))
      (unsupported-op "No such option for " (class obj) ": " attribute))))

(defn do-configure [obj config]
  (doseq [attr-spec config]
    (set-attribute obj attr-spec))
  obj)

(defn configure
  "Configure a component from a set of options. This extracts and executes any special options, then
  configures the component attributes from the remainder"
  [obj config]
  (if (not (instance? Map config))
    (bad-argument "Configuration options must be a Map"))
  (let [errors (validate-config config synthetic-option-specs)]
    (if (not (empty? errors))
      (bad-argument (str/join "\n" errors))))
  (->> config
    (translate-config-keys)
    (do-synthetic-options obj)
    (do-configure obj))
  obj)

