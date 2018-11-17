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
  (println (next mtc)))

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
        (cond (= :INS (first line))
              (let [ins (first (second line))]
                   (cond (= ins :LIST)
                         (show-all @mtc)
                         (= ins :LIST10)
                         (show-all (take 10 @mtc))
                         (= ins :LIST50)
                         (show-all (take 50 @mtc))
                         (= ins :LIST500)
                         (show-all (take 500 @mtc))

                         (= ins :DONE)
                         (swap! mtc done)

                         (= ins :DELAY)
                         (swap! mtc delay)
                         (= ins :DELAY10)
                         (swap! mtc #(delay % 10))
                         (= ins :DELAY50)
                         (swap! mtc #(delay % 50))
                         (= ins :DELAY500)
                         (swap! mtc #(delay % 500))

                         :else
                         (println "Don't understand " ins))
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
