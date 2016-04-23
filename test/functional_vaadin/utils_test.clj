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
  )