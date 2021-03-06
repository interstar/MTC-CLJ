(ns mtc-clj.cli
  (:require
   [clojure.java.io :as io]
   [instaparse.core :as insta]
   [clojure.string :refer [split join]]
   [mtc-clj.core :refer [make-MTC next-item tail add pull pull-one extra
                         reverse-n query
                         add-first delay-item done push-pattern]]
   )
  (:gen-class)
  )



;; ========= USER INTERFACE ============================

(defn show-all [mtc]
  (doseq [item mtc]
    (println item))
  )

(defn show-next [mtc]
  (let [sep (apply str (take 80 (repeat "-")))]
    (do
      (println sep)
      (println "Next : " (next-item mtc))
      (println sep)
      )))



(def parse
  (insta/parser "
ALL = INS | ARGINS | TODO;
INS = DONE | ENDPULL | SAVE | COUNT | QUIT
      | DELAY | LIST
      | DELAYSHORT | DELAYMEDIUM | DELAYLONG
      | LISTSHORT | LISTMEDIUM | LISTLONG;
ARGINS = PULL | PULLONE | REVERSE | PUSHSHORT | PUSHMEDIUM | PUSHLONG | EXTRA | QUERY ;
PULL = PLUS SPACE PATTERN;
PATTERN = NOTSPACE;
PULLONE = DOUBLEPLUS SPACE PATTERN;
REVERSE = 'r' SPACE NUMBER
<SPACE> = #'\\s+';
<NOTSPACE> = #'\\S+';
<PLUS> = '+';
<DOUBLEPLUS> = '++';
QUERY = '?' SPACE PATTERN;
NUMBER = #'[0-9]+'
DELAYLONG = '////';
DELAYMEDIUM = '///';
DELAYSHORT = '//';
DELAY = '/';
DONE = '*';
ENDPULL = '!';
SAVE = \"s\";
COUNT = \"c\";
QUIT = \"q\";
TODO = #'.*';
FTODO = #'.*';
LIST = 'l';
LISTSHORT = 'll';
LISTMEDIUM = 'lll';
LISTLONG = 'llll';
PUSHSHORT = '--' SPACE PATTERN;
PUSHMEDIUM = '---' SPACE PATTERN;
PUSHLONG = '----' SPACE PATTERN;
EXTRA = 'e' SPACE MORE;
MORE = #'.*';
"))



(defmacro safere [expr]
  `(try
     ~expr
    (catch Exception e#
      (println
       (str "Something went wrong. Probably a malformed regex.

Exception was :
") (.getMessage e#)))))

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
                  (safere (swap! mtc #(pull % data)))

                  (= cmd :PULLONE)
                  (safere
                   (swap! mtc #(pull-one % data)))

                  (= cmd :REVERSE)
                  (do
                    (swap! mtc #(reverse-n % data))
                    (println "Reversed first " data " items"))

                  (= cmd :PUSHSHORT)
                  (swap! mtc #(push-pattern % data 10))

                  (= cmd :PUSHMEDIUM)
                  (swap! mtc #(push-pattern % data 50))

                  (= cmd :PUSHLONG)
                  (swap! mtc #(push-pattern % data 500))

                  (= cmd :QUERY)
                  (safere
                    (let [res (query @mtc data)]
                        (println (str "Searching for " data))
                        (doseq [item res]
                          (println (str "> " item))
                          )
                        (println "Total matches :" (count res))
                        (println)
                        ))


                  (= cmd :EXTRA)
                  (swap! mtc #(extra % data))
                  )

            (show-next @mtc))

          (= :INS (first line))
          (let [cmd (first (second line))
                show (fn [x] (show-next @mtc) )]
            (cond (= cmd :LIST)
                  (show-all @mtc)
                  (= cmd :LISTSHORT)
                  (show-all (take 10 @mtc))
                  (= cmd :LISTMEDIUM)
                  (show-all (take 50 @mtc))
                  (= cmd :LISTLONG)
                  (show-all (take 500 @mtc))

                  (= cmd :DONE)
                  (show (swap! mtc done))
                  (= cmd :COUNT)
                  (show (println (count @mtc) " items. In " filename ))
                  (= cmd :SAVE)
                  (do
                    (spit filename (join "\n" @mtc) )
                    (println "Saved ...")
                    (show-next @mtc))

                  (= cmd :QUIT)
                  (do
                    (spit (str "tmp_" filename) (join "\n" @mtc))
                    (println "Quitting ... saved current state in tmp_" filename)
                    (System/exit 0)
                    )

                  (= cmd :ENDPULL)
                  (show (swap! mtc pull))

                  (= cmd :DELAY)
                  (show (swap! mtc delay-item))
                  (= cmd :DELAYSHORT)
                  (show (swap! mtc #(delay-item % 10)))
                  (= cmd :DELAYMEDIUM)
                  (show (swap! mtc #(delay-item % 50)))
                  (= cmd :DELAYLONG)
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

    (let [fname (first args)]
      (if (.exists (io/file fname))
        (let [lines (split (slurp (first args)) #"\n")
              mtc (atom (make-MTC lines))]
          (println "Welcome to Mind Traffic Control")
          (show-next @mtc)
          (while true
            (let [x (read-line)]
              (handle-input (first args) x mtc)
              )))
        (println "Sorry, " fname " does not exist.")))))
