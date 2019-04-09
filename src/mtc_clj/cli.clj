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
  (println "Next : " (next-item mtc)))



(def parse
  (insta/parser "
ALL = INS | ARGINS | TODO;
INS = DONE | ENDPULL | SAVE | COUNT
      | DELAY | LIST
      | DELAYSHORT | DELAYMEDIUM | DELAYLONG
      | LISTSHORT | LISTMEDIUM | LISTLONG;
ARGINS = PULL | PULLONE | REVERSE | PUSHSHORT | PUSHMEDIUM | PUSHLONG | EXTRA | QUERY ;
PULL = PLUS SPACE PATTERN;
PATTERN = NOTSPACE;
PULLONE = PLUS PLUS SPACE PATTERN;
REVERSE = 'r' SPACE N
<SPACE> = #'\\s+';
<NOTSPACE> = #'\\S+';
<PLUS> = '+';
QUERY = '?' SPACE PATTERN;
N = #'[0-9+]'
DELAYLONG = '////';
DELAYMEDIUM = '///';
DELAYSHORT = '//';
DELAY = '/';
DONE = '*';
ENDPULL = '!';
SAVE = \"s\";
COUNT = \"c\";
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

                  (= cmd :PULLONE)
                  (do
                    (println "Pull One not currently implemented.")
                    (swap! mtc #(pull-one % data)))

                  (= cmd :REVERSE)
                  (do
                    (println (str "In REVERSE N. N is " data) )
                    (swap! mtc #(reverse-n % data))
                    (println "___________")
                    (println mtc))

                  (= cmd :PUSHSHORT)
                  (swap! mtc #(push-pattern % data 10))

                  (= cmd :PUSHMEDIUM)
                  (swap! mtc #(push-pattern % data 50))

                  (= cmd :PUSHLONG)
                  (swap! mtc #(push-pattern % data 500))

                  (= cmd :QUERY)
                  (let [res (query @mtc data)]
                    (println (str "Searching for " data))
                    (doseq [item res]
                      (println (str "> " item))))

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
