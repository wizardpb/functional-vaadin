(ns functional-vaadin.utils-test
  (:use [clojure.test]
        [functional-vaadin.utils]))


(deftest all
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