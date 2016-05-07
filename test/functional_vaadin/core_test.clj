(ns functional-vaadin.core-test
  (:use [clojure.test]
        [functional-vaadin.thread-vars]
        [functional-vaadin.core]
        [functional-vaadin.build-support]
        [functional-vaadin.utils]
        )
  (:import (com.vaadin.ui Panel VerticalLayout Button TextField HorizontalLayout FormLayout Label
                          TextArea PasswordField PopupDateField RichTextArea InlineDateField CheckBox
                          Slider CheckBox ComboBox TwinColSelect NativeSelect ListSelect OptionGroup Image Embedded Table)
           (java.util Date)
           (com.vaadin.data.fieldgroup FieldGroup)
           [functional_vaadin.ui TestUI]
           (com.vaadin.data.util IndexedContainer PropertysetItem)))

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
     {#'*current-field-group* (FieldGroup. (PropertysetItem.))}
     ~@forms
     *current-field-group*))

(deftest ui-form-fields
  (testing "Creation"
    (with-form
      (is (instance? TextField (text-field "propId")))
      (is (instance? CheckBox (check-box "checked?")))
      (is (=  "Text Field" (.getCaption (text-field "text-field")))) ; Auto set of caption
      (is (= "Field" (.getCaption (text-field "propId2" {:caption "Field"}))))
      ))
  (testing "Binding"
    (let [fg (with-form
               (text-field "tf1")
               (text-field "tf2" String)
               (text-field "tf3" {:caption "Text Field 3"})
               (text-field "tf4" String {:caption "Text Field 4"})
               (check-box "cb2"))]
      (is (instance? FieldGroup fg))
      (is (= (count (.getFields fg)) 5))
      (is (= (set (map #(.getPropertyId fg %1) (.getFields fg))) #{"tf1" "tf2" "tf3" "tf4" "cb2"}))
      (is (= (set (.getBoundPropertyIds fg)) #{"tf1" "tf2" "tf3" "tf4" "cb2"}))
      (is (= "Text Field 3" (.getCaption (.getField fg "tf3") )))
      (is (= "Text Field 4" (.getCaption (.getField fg "tf4") )))
      (do
        (.setBuffered (.getField fg "tf3") false)
        (.setValue (.getItemProperty (.getItemDataSource fg) "tf3") "Text 3")
        (is (= "Text 3" (.getValue (.getField fg "tf3"))))))
    )
  )


(deftest ui-forms
  (testing "Creation"
    (is (instance? FormLayout (form)))
    (is (instance? FieldGroup (get-field-group (form))))
    (is (instance? VerticalLayout (form {:content VerticalLayout})))
    )
  (testing "Fields"
    (let [form (form
                 (text-field "prop1")
                 (check-box "checked"))]
      (is (= 2 (.getComponentCount form)))
      (is (= [TextField CheckBox] (map #(class (.getComponent form %1)) (range 0 2)))))
    )
  )

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

(deftest ui-tables
  (testing "Creation"
    (let [tbl (table "My Table"
                     (table-column "Col1")
                     (table-column "Col2"))]
      (is (instance? Table tbl))
      (is (= "My Table" (.getCaption tbl)))
      (is (= #{"Col1" "Col2"} (set (.getContainerPropertyIds tbl))))
      ))
  (testing "Column config options"
    (let [tbl (table "My Table"
                     (table-column "Col1" {:type String :defaultValue "" :header "Column 1" :width 100})
                     (table-column "Col2" {:type Integer :defaultValue 0 :header "Column 2" :width 50}))
          itemId (.addItem tbl)]
      (is (instance? Table tbl))
      (is (= "My Table" (.getCaption tbl)))
      (is (= #{"Col1" "Col2"} (set (.getContainerPropertyIds tbl))))
      (is (= [100 50] (map #(.getColumnWidth tbl %1) ["Col1" "Col2"])))
      (is (= [String Integer] (map #(.getType (.getContainerProperty tbl itemId %1)) ["Col1" "Col2"])))
      (is (= ["" 0] (map #(.getValue (.getContainerProperty tbl itemId %1)) ["Col1" "Col2"])))
      ))
  (testing "Data"
    (let [tbl (table "My Table"
                     (table-column "Col1")
                     (table-column "Col2"))
          data (IndexedContainer.)]
      (.addContainerProperty data "Col1" String "")
      (.addContainerProperty data "Col2" Long 0)
      (reduce (fn [c index] (.addItem c index)
                (.setValue (.getContainerProperty c index "Col1") (str "Cell 1," index))
                (.setValue (.getContainerProperty c index "Col2") index)
                c)
              data (range 0 10))
      (.setContainerDataSource tbl data)
      (is (= (.size tbl) 10)
      )
    )))

(deftest ui-building
  (testing "Basic UI"
    (let [ui (defui (TestUI.)
                    (vertical-layout
                      (label "Label 1")
                      (label "Label 2")))]
      (is (instance? TestUI ui))
      (let [vl (.getContent ui)]
        (is (instance? VerticalLayout vl))
        (is (= (.getComponentCount vl) 2))
        (is (every? #(instance? Label %1) (map #(.getComponent vl %1) [0 1])))
        )))

  (testing "Complex UI"
    (let [ui (defui (TestUI.)
                    (panel "Top Panel"
                           (tab-sheet
                             (vertical-layout {:caption "Tab 1"}
                                              (label "Line 1") (label "Line 2")
                                              (label "Line 3") (label "Line 4")
                                              (label "Line 5") (label "Line 6")
                                              (label "Line 7") (label "Line 8")
                                              (label "Line 9") (label "Line 10")
                                              )
                             (panel "Tab 2"
                                    (form
                                      (text-field "name")
                                      (text-field "address1")
                                      (text-field "address2")
                                      (text-field "city")
                                      (text-field "state")))
                             (panel "Tab 3"
                                    (grid-layout 3 4
                                                 (label "R1C1") (label "R1C2") (label "R1C3")
                                                 (label "R2C1") (label "R2C2") (label "R2C3")
                                                 (label "R3C1") (label "R3C2") (label "R3C3")
                                                 (label "R4C1") (label "R4C2") (label "R4C3")
                                                 )
                                    )
                             )
                           )
                    )
          ]
      )))