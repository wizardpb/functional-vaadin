(ns functional-vaadin.builders-test
  (:use [clojure.test]
        [functional-vaadin.builders]
        [functional-vaadin.utils])
  (:import (com.vaadin.ui Panel VerticalLayout Button TextField HorizontalLayout FormLayout Label
                          TextArea PasswordField PopupDateField RichTextArea InlineDateField CheckBox
                          Slider CheckBox ComboBox TwinColSelect NativeSelect ListSelect OptionGroup Image Embedded)
           (java.util Date)
           (com.vaadin.data.fieldgroup FieldGroup)
           (apple.laf JRSUIUtils$Images)))

(deftest ui-panel
  (testing "Building"
    (let [p (panel)]
      (is (instance? Panel p))
      (is (nil? (.getCaption p)))
      (is (nil? (.getContent p))))

    (let [p (panel "Caption")]
      (is (instance? Panel p))
      (is (nil? (.getContent p)))
      (is (= "Caption" (.getCaption p)))
      (is (nil? (.getContent p))))

    (let [p (panel "Caption" (VerticalLayout.))]
      (is (instance? Panel p))
      (is (instance? VerticalLayout (.getContent p)))
      (is (= "Caption" (.getCaption p)))
      (is (= 0 (.getComponentCount (.getContent p)))))

    (let [p (panel {:caption "Caption" :content (VerticalLayout.)})]
      (is (instance? Panel p))
      (is (instance? VerticalLayout (.getContent p)))
      (is (= "Caption" (.getCaption p)))
      (is (= 0 (.getComponentCount (.getContent p)))))

    (let [p (panel "Caption" (VerticalLayout.) (Label.))]
      (is (instance? Panel p))
      (is (instance? VerticalLayout (.getContent p)))
      (is (= "Caption" (.getCaption p)))
      (is (= 1 (.getComponentCount (.getContent p))))
      (is (instance? Label (.getComponent (.getContent p) 0)))
      )

    (let [p (panel {:caption "Caption" :content (VerticalLayout.)} (Label.))]
      (is (instance? Panel p))
      (is (instance? VerticalLayout (.getContent p)))
      (is (= "Caption" (.getCaption p)))
      (is (= 1 (.getComponentCount (.getContent p))))
      (is (instance? Label (.getComponent (.getContent p) 0)))
      )
    )
  )

(deftest ui-ordered-layout
  (testing "Building"
    (doseq [[fn cls] [[vertical-layout VerticalLayout]
                      [horizontal-layout HorizontalLayout]
                      [form-layout FormLayout]]]
      (let [layout (apply fn [])]
        (is (instance? cls layout))
        (is (= 0 (.getComponentCount layout))))

      (let [layout (apply fn [(TextField.) (Button.)])]
        (is (instance? cls layout))
        (is (= 2 (.getComponentCount layout)))
        (is (instance? TextField (.getComponent layout 0)))
        (is (instance? Button (.getComponent layout 1))))

      (let [t (TextField.)
            b (Button.)
            layout (apply fn [ t b])]
        (is (= 2 (.getComponentCount layout)))
        (is (identical? t (.getComponent layout 0)))
        (is (identical? b (.getComponent layout 1))))

      (let [vt (apply fn [(button {:caption "Push Me"}) (label "Text")])
            b (.getComponent vt 0)
            l (.getComponent vt 1)]
        (is (and (instance? Button b) (instance? Label l)))
        (is (= (.getCaption b) "Push Me"))
        (is (= (.getValue l) "Text"))))
    )
  (testing "Parent options"
    (let [vt (vertical-layout (button {:caption "Push Me" :expandRatio 0.5}))
          b (.getComponent vt 0)]
      (is (= (.getExpandRatio vt b) 0.5)))))

(deftest ui-grid-layout
  (testing "Building"
    (let [layout (grid-layout
                   (label "label") (button "Push Me"))]
      (is (= 2 (.getComponentCount layout)))
      (is (= 2 (.getRows layout)))
      (is (= 1 (.getColumns layout))))
    (let [layout (grid-layout 1 2
                              (label {:caption "label" :position [0 0]})
                              (button {:caption "Push Me" :position [0 1]}))]
      (is (= 2 (.getComponentCount layout)))
      (is (= 2 (.getRows layout)))
      (is (= 1 (.getColumns layout))))
    ))

(deftest ui-split-panels
  (testing "Building"
    (doseq [panel [(vertical-split-panel (vertical-layout) (horizontal-layout))
                   (horizontal-split-panel (vertical-layout) (horizontal-layout))]]
      (is (= 2 (.getComponentCount panel)))
      (is (instance? VerticalLayout (.getFirstComponent panel)))
      (is (instance? HorizontalLayout (.getSecondComponent panel))))
    (doseq [panel [(vertical-split-panel {:firstComponent (vertical-layout)
                                          :secondComponent (horizontal-layout)})
                   (horizontal-split-panel (vertical-layout) (horizontal-layout))]]
      (is (= 2 (.getComponentCount panel)))
      (is (instance? VerticalLayout (.getFirstComponent panel)))
      (is (instance? HorizontalLayout (.getSecondComponent panel)))))
  (testing "Validation"
    (is (thrown-with-msg? UnsupportedOperationException #"Split panel can contain only two components"
                          (vertical-split-panel {:firstComponent  (vertical-layout)
                                                 :secondComponent (horizontal-layout)}
                                                (button)))))
  )

(deftest ui-text-fields
  (testing "Building"
    (doseq [[fn cls] [[text-field TextField]
                      [password-field PasswordField]
                      [text-area TextArea]
                      [rich-text-area RichTextArea]]]
      (is (instance? cls (fn)))
      (is (= (.getCaption (fn {:caption "Field"})) "Field"))
      (is (= (.getValue (fn {:value "Content"})) "Content"))
      (is (= (.getCaption (fn "Field")) "Field"))
      (is (= (.getCaption (fn "Field" "Content")) "Field"))
      (is (= (.getValue (fn "Field" "Content")) "Content"))
      ))
  )

(deftest ui-select-fields
  (testing "Building"
    (doseq [[fn klass] [
                        [slider Slider]
                        [check-box CheckBox]
                        [combo-box ComboBox]
                        [twin-col-select TwinColSelect]
                        [native-select NativeSelect]
                        [list-select ListSelect]
                        [option-group OptionGroup]
                        ]]
      (is (instance? klass (fn)))
      (is (= (.getCaption (fn {:caption "Field"})) "Field"))
      (is (= (.getCaption (fn "Field")) "Field"))
      )))

(deftest ui-date-fields
  (testing "Building"
    (doseq [[fn cls] [[popup-date-field PopupDateField]
                      [inline-date-field InlineDateField]]]
      (is (instance? cls (fn {})))
      (is (= (.getCaption (fn {:caption "Field"})) "Field"))
      (is (= (.getValue (fn {:value (Date. 0)})) (Date. 0)))
      (is (= (.getCaption (fn "Field")) "Field"))
      (is (= (.getCaption (fn "Field" (Date. 0))) "Field"))
      (is (= (.getValue (fn "Field" (Date. 0))) (Date. 0)))
      )))

(defmacro with-form [& forms]
  `(with-bindings
     {#'*current-field-group* (FieldGroup.)}
     ~@forms
     *current-field-group*))

(deftest ui-form-fields
  (testing "Creation"
    (with-form
      (is (instance? TextField (form-field "propId" TextField)))
      (is (instance? CheckBox (form-field "checked?" CheckBox)))
      (is (=  "Text Field" (.getCaption (form-field "text-field" TextField)))) ; Auto set of caption
      (is (= "Field" (.getCaption (form-field "propId2" TextField {:caption "Field"}))))
      ))
  (testing "Binding"
    (let [fg (with-form
               (form-field "f1" TextField)
               (form-field "f2" CheckBox))]
      (is (instance? FieldGroup fg))
      (is (= (count (.getFields fg)) 2))
      (is (= (set (map #(.getPropertyId fg %1) (.getFields fg))) #{"f1" "f2"})))
    )
  (testing "Validation"
    (is (thrown-with-msg? UnsupportedOperationException #"Form fields cannot be created outside of forms"
                          (form-field "propId" TextField)))
    (is (thrown-with-msg? IllegalArgumentException #"Form field can only be created from instances of interface com.vaadin.ui.Field"
                          (with-form
                            (form-field "propId" (Object.)))))))

(deftest ui-forms
  (testing "Creation"
    (is (instance? FormLayout (form)))
    (is (instance? FieldGroup (get-data (form) :field-group)))
    (is (instance? VerticalLayout (form {:content VerticalLayout})))
    ))

(deftest ui-embedded
  (testing "Image"
    (is (instance? Image (image)))
    (is (= "Caption" (.getCaption (image "Caption"))))
    (is (= "Caption" (.getCaption (image {:caption "Caption"}))))
    )
  (testing "Embedded"
    (is (instance? Embedded (embedded)))
    (is (= "Caption" (.getCaption (embedded "Caption"))))
    (is (= "Caption" (.getCaption (embedded {:caption "Caption"}))))
    )
  )

