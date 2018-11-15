(ns mtc-clj.core-test
  (:require [clojure.test :refer :all]
            [mtc-clj.core :refer :all]))


(deftest basic-queue
  (testing "basic queue functions"
    (let [mtc '("item 1" "item 2" "item 3")
          mtc2 (add mtc "item 4")
          mtc3 (add mtc2 "item 5")]

      (is (= (count '()) 0))
      (is (= (count mtc) 3))
      (is (= (count mtc3) 5))
      (is (= (next mtc) "item 1"))
      (is (= (tail mtc) '("item 2" "item 3")))

      (is (= mtc2 '("item 1" "item 2" "item 3" "item 4")))
      (is (= mtc3 '("item 1" "item 2" "item 3" "item 4" "item 5")))
      (is (= (add-first mtc "item 0") '("item 0" "item 1" "item 2" "item 3")))

      (is (= (delay mtc) '("item 2" "item 3" "item 1")))
      (is (= (done mtc) '("item 2" "item 3")))
      ) ))

(deftest defering
  (testing "delay"
    (let [mtc (range 10)]
      (is (= (delay mtc 5) '(1 2 3 4 5 0 6 7 8 9) ))
      )))

(deftest tags
  (testing "+project tags"
    (let [mtc (make-MTC '("hello" "teenage +america" "world" "team +america police"))]
      (is (= (pull mtc #"\+america")
             '("teenage +america" "team +america police" "hello" "world"))))))
