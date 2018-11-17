(ns mtc-clj.cli
  (:require
   [instaparse.core :as insta]
   [mtc-clj.core :refer [make-MTC next tail add pull delay done]]
   ))



;; ========= USER INTERFACE ============================

(defn show-all [mtc]
  (doseq [item mtc]
    (println item))
  )

(defn show-next [mtc]
  (println "Next : " (next mtc)))



(def parse
  (insta/parser "
ALL = INS | ARGINS | TODO;
INS = DONE | ENDPULL | DELAY | LIST
      | DELAY10 | DELAY50 | DELAY500
      | LIST10 | LIST50 | LIST500;
ARGINS = PULL | FRONTADD;
PULL = PLUS SPACE PATTERN;
PATTERN = NOTSPACE;
<FRONTADD> = ENDPULL SPACE FTODO;
<SPACE> = #'\\s+';
<NOTSPACE> = #'\\S+';
<PLUS> = '+';
DELAY500 = '////';
DELAY50 = '///';
DELAY10 = '//';
DELAY = '/';
DONE = '*';
ENDPULL = '\\\\';
TODO = #'.*';
FTODO = #'.*';
LIST = 'l';
LIST10 = 'll';
LIST50 = 'lll';
LIST500 = 'llll';
"))

(defn handle-input [input mtc]
  (let [parsed (insta/parses parse input) ]
    (if (= 1 (count parsed))
      (do
        (swap! mtc add input)
        (show-next @mtc))
      (let [line (second (second parsed))]
        (cond
          (= :ARGINS (first line))
          (let [cmd (-> line second first)
                pattern (-> line second (#(nth % 3)) second)]
            (println "Argument ins " cmd " : " pattern)
            (cond (= cmd :PULL)
                  (swap! mtc #(pull % pattern)))
            (show-next @mtc))

          (= :INS (first line))
          (let [cmd (first (second line))]
            (cond (= cmd :LIST)
                  (show-all @mtc)
                  (= cmd :LIST10)
                  (show-all (take 10 @mtc))
                  (= cmd :LIST50)
                  (show-all (take 50 @mtc))
                  (= cmd :LIST500)
                  (show-all (take 500 @mtc))

                  (= cmd :DONE)
                  (swap! mtc done)

                  (= cmd :DELAY)
                  (swap! mtc delay)
                  (= cmd :DELAY10)
                  (swap! mtc #(delay % 10))
                  (= cmd :DELAY50)
                  (swap! mtc #(delay % 50))
                  (= cmd :DELAY500)
                  (swap! mtc #(delay % 500))

                  :else
                  (println "Don't understand " cmd))
            (show-next @mtc)
            ))

        ))))

(defn -main [& args]
  (let [mtc (atom (make-MTC '()))]
    (println "Welcome to Mind Traffic Control")
    (while true
      (let [x (read-line)]
        (handle-input x mtc)
        ))))
