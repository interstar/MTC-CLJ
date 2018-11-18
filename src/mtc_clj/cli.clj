(ns mtc-clj.cli
  (:require
   [instaparse.core :as insta]
   [clojure.string :refer [split join]]
   [mtc-clj.core :refer [make-MTC next-item tail add pull
                         add-first delay-item done]]
   )
  (:gen-class)
  )



;; ========= USER INTERFACE ============================

(defn show-all [mtc]
  (doseq [item mtc]
    (println item))
  )

(defn show-next [mtc]
  (println "Next : " (next-item mtc)))



(def parse
  (insta/parser "
ALL = INS | ARGINS | TODO;
INS = DONE | ENDPULL | SAVE | COUNT
      | DELAY | LIST
      | DELAY10 | DELAY50 | DELAY500
      | LIST10 | LIST50 | LIST500;
ARGINS = PULL ;
PULL = PLUS SPACE PATTERN;
PATTERN = NOTSPACE;
<SPACE> = #'\\s+';
<NOTSPACE> = #'\\S+';
<PLUS> = '+';
DELAY500 = '////';
DELAY50 = '///';
DELAY10 = '//';
DELAY = '/';
DONE = '*';
ENDPULL = '!!';
SAVE = \"s\";
COUNT = \"c\";
TODO = #'.*';
FTODO = #'.*';
LIST = 'l';
LIST10 = 'll';
LIST50 = 'lll';
LIST500 = 'llll';
"))

(defn handle-input [filename input mtc]
  (let [parsed (insta/parses parse input) ]
    (if (= 1 (count parsed))
      (cond
        (empty? input) (show-next @mtc)
        (< (count input) 5)
        (do
          (println "Ignoring input " input " as it's too short.")
          (show-next @mtc))
        :else
        (do
          (swap! mtc add-first input)
          (show-next @mtc)))
      (let [line (second (second parsed))]
        (cond
          (= :ARGINS (first line))
          (let [cmd (-> line second first)
                data (-> line second (#(nth % 3)) second)]

            (cond (= cmd :PULL)
                  (swap! mtc #(pull % data))

                  )

            (show-next @mtc))

          (= :INS (first line))
          (let [cmd (first (second line))
                show (fn [x] (show-next @mtc) )]
            (cond (= cmd :LIST)
                  (show-all @mtc)
                  (= cmd :LIST10)
                  (show-all (take 10 @mtc))
                  (= cmd :LIST50)
                  (show-all (take 50 @mtc))
                  (= cmd :LIST500)
                  (show-all (take 500 @mtc))

                  (= cmd :DONE)
                  (show (swap! mtc done))
                  (= cmd :COUNT)
                  (show (println (count @mtc) " items." ))
                  (= cmd :SAVE)
                  (do
                    (spit filename (join "\n" @mtc) )
                    (println "Saved ...")
                    (show-next @mtc))

                  (= cmd :ENDPULL)
                  (show (swap! mtc pull))

                  (= cmd :DELAY)
                  (show (swap! mtc delay-item))
                  (= cmd :DELAY10)
                  (show (swap! mtc #(delay-item % 10)))
                  (= cmd :DELAY50)
                  (show (swap! mtc #(delay-item % 50)))
                  (= cmd :DELAY500)
                  (show (swap! mtc #(delay-item % 500)))

                  :else
                  (show (println "Don't understand " cmd)))
            ))

        ))))

(defn -main [& args]
  (if (empty? args)
    (println "Welcome to Mind Traffic Control

Mind Traffic Control works with a plain-text file of to-do items, typically called something like todo.txt.

Please give the path to such a file as an argument when running this program.

Eg.

lein run ~/Documents/todos/todo.txt

 ")
    (let [lines (split (slurp (first args)) #"\n")
          mtc (atom (make-MTC lines))]
      (println "Welcome to Mind Traffic Control")
      (show-next @mtc)
      (while true
        (let [x (read-line)]
          (handle-input (first args) x mtc)
          )))))
