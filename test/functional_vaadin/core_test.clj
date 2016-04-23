(ns functional-vaadin.core-test
  (:use [clojure.test]
        [functional-vaadin.core])
  (:import (com.vaadin.ui Panel VerticalLayout Button TextField HorizontalLayout FormLayout Label TextArea PasswordField PopupDateField RichTextArea InlineDateField)
           (java.util Date)))

(deftest ui-panel
  (testing "Building"
    (let [panel (panel {})]
      (is (instance? Panel panel))
      (is (nil? (.getContent panel))))
    (let [panel (panel {:content (VerticalLayout.)})]
      (is (instance? Panel panel))
      (is (instance? VerticalLayout (.getContent panel))))
    (is (thrown-with-msg?
          UnsupportedOperationException #"Cannot add children to an instance of class com.vaadin.ui.Button"
          (panel {:content (Button.)})))))

(deftest ui-ordered-layout
  (testing "Building"
    (doseq [[fn cls] [[vertical-layout VerticalLayout]
                [horizontal-layout HorizontalLayout]
                [form-layout FormLayout]]]
      (let [layout (apply fn {} [])]
        (is (instance? cls layout))
        (is (= 0 (.getComponentCount layout))))

      (let [layout (apply fn [{} (TextField.) (Button.)])]
        (is (instance? cls layout))
        (is (= 2 (.getComponentCount layout)))
        (is (instance? TextField (.getComponent layout 0)))
        (is (instance? Button (.getComponent layout 1))))

      (let [t (TextField.)
            b (Button.)
            layout (apply fn [{} t b])]
        (is (= 2 (.getComponentCount layout)))
        (is (identical? t (.getComponent layout 0)))
        (is (identical? b (.getComponent layout 1))))

      (let [vt (apply fn [{} (button {:caption "Push Me"}) (label "Text")])
            b (.getComponent vt 0)
            l (.getComponent vt 1)]
        (is (and (instance? Button b) (instance? Label l)))
        (is (= (.getCaption b) "Push Me"))
        (is (= (.getValue l) "Text"))))
    )
  (testing "Parent options"
    (let [vt (vertical-layout {} (button {:caption "Push Me" :expandRatio 0.5}))
          b (.getComponent vt 0)]
      (is (= (.getExpandRatio vt b) 0.5)))))

(deftest ui-text-fields
  (testing "Building"
    (doseq [[fn cls] [[text-field TextField]
                      [password-field PasswordField]
                      [text-area TextArea]
                      [rich-text-area RichTextArea]]]
      (is (instance? cls (fn {})))
      (is (= (.getCaption (fn {:caption "Field"})) "Field"))
      (is (= (.getValue (fn {:value "Content"})) "Content"))
      (is (= (.getCaption (fn "Field")) "Field"))
      (is (= (.getCaption (fn "Field" "Content")) "Field"))
      (is (= (.getValue (fn "Field" "Content")) "Content"))
      (is (thrown-with-msg?
            IllegalArgumentException #"Both arguments must be Strings"
            (fn {} "Text")))
      (is (thrown-with-msg?
            IllegalArgumentException #"Too many arguments for .*"
            (fn {} "Text" (Button.))))
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
      (is (thrown-with-msg?
            IllegalArgumentException #"Arguments must be a String and a Date"
            (fn {} "Text")))
      (is (thrown-with-msg?
            IllegalArgumentException #"Too many arguments for .*"
            (fn {} "Text" (Button.))))
      )))

