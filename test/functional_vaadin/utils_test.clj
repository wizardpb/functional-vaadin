(ns functional-vaadin.utils-test
  (:use [clojure.test]
        [functional-vaadin.utils])
  (:import (java.util Map)
           (com.vaadin.ui Button)))


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