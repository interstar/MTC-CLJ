(ns mtc-clj.core

  (:require [clojure.spec.alpha :as s]
            ;; [clojure.data.finger-tree :refer [double-list]]

            )
  )


(s/def ::MTC (s/coll-of string?))

(defn make-MTC [items]
  (s/conform ::MTC items))

(defn next-item [mtc]
  (if (empty? mtc) "Mind Traffic Control is currently empty"
      (first mtc)))
(defn tail [mtc] (rest mtc))
(defn add [mtc item] (lazy-seq (concat mtc (list item) )))
(defn add-first [mtc item] (lazy-seq (cons item mtc)))


(defn delay-item
  ([mtc] (lazy-seq (concat (tail mtc) (list  (next-item mtc)))) )
  ([mtc n]
   (let [x  (next-item mtc)
         as (take n (rest mtc))
         zs (drop n (rest mtc))]
     (lazy-seq (concat as (list x) zs)))) )

(defn done [mtc] (tail mtc))

(defn pull
  ([mtc]
   (let [sting (last mtc)]
     (lazy-seq (concat (list sting) (butlast mtc))))
   )
  ([mtc pattern]
   (let [nf #(nil? (re-find (re-pattern pattern) %))
         no-match (filter nf mtc )
         match (filter #(not (nf %)) mtc )
         ]
     (lazy-seq (concat match no-match))
     )))



(defn info [mtc pattern]
  (let [nf #(nil? (re-find (re-pattern pattern) %))
        no-match (filter nf mtc)
        match (filter #(not (nf %)) mtc)
        item-filt #(nil? (re-find (re-pattern (str "^&" pattern)) % ) )
        items (filter item-filt match)
        not-items (filter #(not (item-filt %)) match)
        ]
    (lazy-seq (concat items not-items))))
