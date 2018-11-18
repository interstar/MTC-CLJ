(ns mtc-clj.core-test
  (:require [clojure.test :refer :all]
            [mtc-clj.core :refer :all]

            )
  (:gen-class)
  )


(deftest basic-queue
  (testing "basic queue functions"
    (let [mtc '("item 1" "item 2" "item 3")
          mtc2 (add mtc "item 4")
          mtc3 (add mtc2 "item 5")]

      (is (= (count '()) 0))
      (is (= (count mtc) 3))
      (is (= (count mtc3) 5))
      (is (= (next-item '()) "Mind Traffic Control is currently empty"))
      (is (= (next-item mtc) "item 1"))
      (is (= (tail mtc) '("item 2" "item 3")))

      (is (= mtc2 '("item 1" "item 2" "item 3" "item 4")))
      (is (= mtc3 '("item 1" "item 2" "item 3" "item 4" "item 5")))
      (is (= (add-first mtc "item 0") '("item 0" "item 1" "item 2" "item 3")))

      (is (= (delay-item mtc) '("item 2" "item 3" "item 1")))
      (is (= (done mtc) '("item 2" "item 3")))
      ) ))

(deftest defering
  (testing "delay"
    (let [mtc (range 10)]
      (is (= (delay-item mtc 5) '(1 2 3 4 5 0 6 7 8 9) ))
      )))

(deftest tags
  (testing "+project tags and @places"
    (let [mtc (make-MTC '("hello" "teenage +america" "world" "team +america police" "@email me"))]
      (is (= (pull mtc #"\+america")
             '("teenage +america" "team +america police" "hello" "world" "@email me")) )
      (is (= (pull mtc #"@email")
             '("@email me" "hello" "teenage +america" "world" "team +america police"))))
    ))


(deftest infos
  (testing "&information tags"
    (let [mtc (make-MTC '("item1" "item2 +tag" "&tag info"
                          "item3" "item4 +tag"))]
      (is (= (info mtc #"tag")
             '("item2 +tag" "item4 +tag" "&tag info") ))
      )
    ))
