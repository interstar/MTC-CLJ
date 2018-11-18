(ns mtc-clj.core-test
  (:require [clojure.test :refer :all]
            [mtc-clj.core :refer :all]
            [mtc-clj.cli :refer [parse]]
            [instaparse.core :as insta]
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

(deftest pushing
  (testing "push pattern"
    (let [mtc (make-MTC '("hello1" "teenage" "america1" "goodbye" "cruel" "world" ))]
      (is (= (push-pattern mtc "2" 3)
             '("hello1" "teenage" "america1" "goodbye" "cruel" "world" )
             ))
      (is (= (push-pattern mtc "1" 3)
             '("teenage" "goodbye" "cruel" "hello1" "america1" "world"))))))

(deftest tags
  (testing "+project tags and @places"
    (let [mtc (make-MTC '("hello" "teenage +america" "world" "team +america police" "@email me"))]
      (is (= (pull mtc #"\+america")
             '("teenage +america" "team +america police" "hello" "world" "@email me")) )
      (is (= (pull mtc #"@email")
             '("@email me" "hello" "teenage +america" "world" "team +america police"))))
    ))


(deftest extra-top
  (testing "append to the current next-item"
    (let [mtc (make-MTC '("hello" "world"))]
      (is (= (extra mtc "teenage america")
             '("hello teenage america" "world"))))))

(deftest infos
  (testing "&information tags"
    (let [mtc (make-MTC '("item1" "item2 +tag" "&tag info"
                          "item3" "item4 +tag"))]
      (is (= (info mtc #"tag")
             '("item2 +tag" "item4 +tag" "&tag info") ))
      )
    ))


(deftest grammar
  (testing "grammar"
    (let [prs #(insta/parses parse %)]
      (is (= (prs "hello world")
             (list [:ALL [:TODO "hello world"]])))

      (is (= (prs "+ hello")
             (list [:ALL [:TODO "+ hello"]]
                   [:ALL [:ARGINS [:PULL "+" " " [:PATTERN "hello"]]]]
                   )))

      (is (= (prs "-- goodbye")
             (list [:ALL [:TODO "-- goodbye"]]
                   [:ALL [:ARGINS [:PUSHSHORT "--" " " [:PATTERN "goodbye"]]]]
                   )))

      (is (= (prs "--- goodbye")
             (list [:ALL [:TODO "--- goodbye"]]
                   [:ALL [:ARGINS [:PUSHMEDIUM "---" " " [:PATTERN "goodbye"]]]]
                   )))

      (is (= (prs "---- goodbye")
             (list [:ALL [:TODO "---- goodbye"]]
                   [:ALL [:ARGINS [:PUSHLONG "----" " " [:PATTERN "goodbye"]]]]
                   )))


      (is (= (prs "e mo' better blues")
             (list [:ALL [:TODO "e mo' better blues"]]
                   [:ALL [:ARGINS [:EXTRA "e" " " [:MORE "mo' better blues"]] ]])))

      (is (= (prs "*")
             (list [:ALL [:TODO "*"]]
                   [:ALL [:INS [:DONE "*"]]])))

      (is (= (prs "!")
             (list [:ALL [:TODO "!"]]
                   [:ALL [:INS [:ENDPULL "!"]]])))

      (is (= (prs "c")
             (list [:ALL [:TODO "c"]]
                   [:ALL [:INS [:COUNT "c"]]])))

      (is (= (prs "s")
             (list [:ALL [:TODO "s"]]
                   [:ALL [:INS [:SAVE "s"]]])))




      (is (= (prs "/")
             (list [:ALL [:TODO "/"]]
                   [:ALL [:INS [:DELAY "/"]]])))

      (is (= (prs "//")
             (list [:ALL [:TODO "//"]]
                   [:ALL [:INS [:DELAYSHORT "//"]]])
             ))

      (is (= (prs "///")
             (list [:ALL [:TODO "///"]]
                   [:ALL [:INS [:DELAYMEDIUM "///"]]])
             ))

      (is (= (prs "////")
             (list [:ALL [:TODO "////"]]
                   [:ALL [:INS [:DELAYLONG "////"]]])
             ))


      (is (= (prs "l")
             (list [:ALL [:TODO "l"]]
                   [:ALL [:INS [:LIST "l"]]])))

      (is (= (prs "ll")
             (list [:ALL [:TODO "ll"]]
                   [:ALL [:INS [:LISTSHORT "ll"]]])))

      (is (= (prs "lll")
             (list [:ALL [:TODO "lll"]]
                   [:ALL [:INS [:LISTMEDIUM "lll"]]])))

      (is (= (prs "llll")
             (list [:ALL [:TODO "llll"]]
                   [:ALL [:INS [:LISTLONG "llll"]]])))


      )))
