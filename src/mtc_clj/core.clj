(ns mtc-clj.core

  (:require [clojure.spec.alpha :as s]
           ; [clojure.data.finger-tree :refer [double-list]]
            )
  )


(s/def ::MTC (s/coll-of string?))

(defn make-MTC [items]
  (s/conform ::MTC items))

(defn next [mtc] (first mtc))
(defn tail [mtc] (rest mtc))
(defn add [mtc item] (lazy-seq (concat mtc (list item) )))
(defn add-first [mtc item] (cons item mtc))


(defn delay
  ([mtc] (lazy-seq (concat (tail mtc) (list  (next mtc)))) )
  ([mtc n]
   (let [x  (next mtc)
         as (take n (rest mtc))
         zs (drop n (rest mtc))]
     (lazy-seq (concat as (list x) zs)))) )

(defn done [mtc] (tail mtc))

(defn pull [mtc pattern]
  (let [nf #(nil? (re-find (re-pattern pattern) %))
        no-match (filter nf mtc )
        match (filter #(not (nf %)) mtc )
        ]
    (lazy-seq (concat match no-match))
    ))
