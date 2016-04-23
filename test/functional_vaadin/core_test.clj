(ns functional-vaadin.core-test
  (:use [clojure.test]
        [functional-vaadin.core])
  (:import (com.vaadin.ui Panel VerticalLayout Button TextField HorizontalLayout FormLayout Label)))

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
    (doseq [td [[vertical-layout VerticalLayout]
                [horizontal-layout HorizontalLayout]
                [form-layout FormLayout]]]
      (let [layout (apply (first td) {} [])]
        (is (instance? (second td) layout))
        (is (= 0 (.getComponentCount layout))))

      (let [layout (apply (first td) [{} (TextField.) (Button.)])]
        (is (instance? (second td) layout))
        (is (= 2 (.getComponentCount layout)))
        (is (instance? TextField (.getComponent layout 0)))
        (is (instance? Button (.getComponent layout 1))))

      (let [t (TextField.)
            b (Button.)
            layout (apply (first td) [{} t b])]
        (is (= 2 (.getComponentCount layout)))
        (is (identical? t (.getComponent layout 0)))
        (is (identical? b (.getComponent layout 1))))

      (let [vt (apply (first td) [{} (button {:caption "Push Me"}) (label "Text")])
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

