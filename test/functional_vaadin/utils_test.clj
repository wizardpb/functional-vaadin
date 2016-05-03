(ns functional-vaadin.utils-test
  (:use [clojure.test]
        [functional-vaadin.utils])
  (:import (java.util Map)
           (com.vaadin.ui Button)
           (functional_vaadin.ui TestUI)))


(deftest string-helpers
  (testing "Capitalization"
    (is (= (capitalize "capCamel") "CapCamel"))
    (is (= (capitalize "CapCamel") "CapCamel"))
    (is (= (uncapitalize "capCamel") "capCamel"))
    (is (= (uncapitalize "CapCamel") "capCamel"))
    )
  (testing "Capitalization - empty string and nil"
    (is (= (capitalize "") ""))
    (is (= (capitalize nil) nil))
    (is (= (uncapitalize "") ""))
    (is (= (uncapitalize nil) nil))
    ))

(deftest component-data
  (testing "attach and get"
    (let [c (Button.)]
      (attach-data c :test-key {:a 1 :b 2})
      (is (= (get-data c :test-key) {:a 1 :b 2}))
      (is (= (get-data c :test-key) {:a 1 :b 2}))           ; Duplicate get to test it does not disapear
      ))
  (testing "attach and detach"
    (let [c (Button.)]
      (attach-data c :test-key {:a 1 :b 2})
      (is (= (detach-data c :test-key) {:a 1 :b 2}))
      (is (nil? (get-data c :test-key)))
      ))
  (testing "attach and get - mutiple/vector keys"
    (let [c (Button.)]
      (attach-data c [:test-key] {:a 1 :b 2})
      (is (= (get-data c [:test-key]) {:a 1 :b 2}))
      (is (= (get-data c [:test-key]) {:a 1 :b 2}))
      (is (= (get-data c [:test-key :a]) 1))
      (is (= (get-data c [:test-key :b]) 2))
      )
    (let [c (Button.)]
      (attach-data c [:test-key :a] 1)
      (attach-data c [:test-key :b] 2)
      (is (= (get-data c [:test-key]) {:a 1 :b 2}))
      (is (= (get-data c [:test-key]) {:a 1 :b 2}))
      (is (= (get-data c [:test-key :a]) 1))
      (is (= (get-data c [:test-key :b]) 2))
      ))
  (testing "attach and detach - multiple/vector keys"
    (let [c (Button.)]
      (attach-data c :test-key {:a 1 :b 2})
      (is (= (detach-data c [:test-key]) {:a 1 :b 2}))
      (is (nil? (get-data c [:test-key])))

      (attach-data c :test-key {:a 1 :b 2})
      (is (= (detach-data c [:test-key :a]) 1))
      (is (= (get-data c [:test-key :b]) 2))
      (is (nil? (get-data c [:test-key :a])))
      (is (= (detach-data c [:test-key :b]) 2))
      (is (nil? (get-data c [:test-key :b])))
      (is (nil? (get-data c [:test-key :a])))
      (is (= (get-data c [:test-key]) {}))
      (is (= (detach-data c :test-key) {}))
      (is (nil? (detach-data c :test-key)))
      ))
  (testing "attach and detach - dot keyword keys"
    (let [c (Button.)]
      (attach-data c :test-key {:a 1 :b 2})
      (is (= (detach-data c :test-key.a) 1))
      (is (= (get-data c :test-key.b) 2))
      (is (nil? (get-data c :test-key.a)))
      (is (= (detach-data c :test-key.b) 2))
      (is (nil? (get-data c :test-key.b)))
      (is (nil? (get-data c :test-key.a)))
      (is (= (detach-data c :test-key) {}))
      (is (nil? (detach-data c :test-key)))
      ))
  (testing "attach and detach - dot String keys"
    (let [c (Button.)]
      (attach-data c "test-key" {:a 1 :b 2})
      (is (= (detach-data c "test-key.a") 1))
      (is (= (get-data c "test-key.b") 2))
      (is (nil? (get-data c "test-key.a")))
      (is (= (detach-data c "test-key.b") 2))
      (is (nil? (get-data c "test-key.b")))
      (is (nil? (get-data c "test-key.a")))
      (is (= (detach-data c "test-key") {}))
      (is (nil? (detach-data c "test-key")))
      ))
  (testing "Key equivalence"
    (let [c (Button.)]
      (attach-data c "test-key" {:a 1 :b 2})
      (is (= (get-data c "test-key.a") 1))
      (is (= (get-data c "test-key.b") 2))
      (is (= (get-data c :test-key.a) 1))
      (is (= (get-data c :test-key.b) 2))
      (is (= (get-data c ["test-key" "a"]) 1))
      (is (= (get-data c ["test-key" "b"]) 2))
      (is (= (get-data c [:test-key :a]) 1))
      (is (= (get-data c [:test-key :b]) 2))
      ))

  )
(deftest ui-data
  (testing "attach/get - components"
    (let [ui (TestUI.)
          c (Button.)]
      (attach-data ui (component-key :button) c)
      (is (identical? c (get-data ui (component-key :button))))
      (is (identical? c (get-data ui (component-key :button))))
      ))
  (testing "attach/detach - components"
    (let [ui (TestUI.)
          c (Button.)]
      (attach-data ui (component-key :button) c)
      (is (identical? c (get-data ui (component-key :button))))
      (is (identical? c (detach-data ui (component-key :button))))
      (is (nil? (get-data ui (component-key :button))))
      )
    )
  (testing "attach/get - bindings"
    (let [ui (TestUI.)
          c {:structure :Map
             :bind-type :Item}]
      (attach-data ui (binding-key :some.data) c)
      (is (identical? c (get-data ui (binding-key :some.data))))
      (is (identical? c (get-data ui (binding-key :some.data))))
      ))
  (testing "attach/detach - bindings"
    (let [ui (TestUI.)
          c {:structure :Map
             :bind-type :Item}]
      (attach-data ui (binding-key :some.data) c)
      (is (identical? c (get-data ui (binding-key :some.data))))
      (is (identical? c (detach-data ui (binding-key :some.data))))
      (is (nil? (get-data ui (binding-key :some.data))))
      )
    )
  )