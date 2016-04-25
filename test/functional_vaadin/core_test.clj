(ns functional-vaadin.core-test
  (:use [clojure.test]
        [functional-vaadin.core])
  (:import (com.vaadin.ui Panel VerticalLayout Button TextField HorizontalLayout FormLayout Label TextArea PasswordField PopupDateField RichTextArea InlineDateField)
           (java.util Date)))

(deftest ui-panel
  (testing "Building"
    (is (instance? Panel (panel)))
    (is (nil? (.getContent (panel))))
    (is (nil? (.getCaption (panel))))
    (is (instance? Panel (panel "Caption")))
    (is (nil? (.getContent (panel "Caption"))))
    (is (= "Caption" (.getCaption (panel "Caption"))))
    (is (instance? Panel (panel "Caption" (VerticalLayout.))))
    (is (instance? VerticalLayout (.getContent (panel "Caption" (VerticalLayout.)))))
    (is (= "Caption" (.getCaption (panel "Caption" (VerticalLayout.)))))
    ))

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
    ;(let [layout (grid-layout 1 2
    ;               (label {:caption "label" :position [0 0]})
    ;               (button {:caption "Push Me" :position [0 1]}))]
    ;  (is (= 2 (.getComponentCount layout)))
    ;  (is (= 1 (.getRows layout)))
    ;  (is (= 2 (.getColumns layout))))
    ))

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

