(defproject mtc-clj "0.2.0-SNAPSHOT"
  :description "Mind Traffic Control - In Clojure"
  :url "https://github.com/interstar/MTC-CLJ"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.finger-tree "0.0.3"]
                 [instaparse "1.4.9"]
                 ]
  :aot [mtc-clj.cli]
  :main mtc-clj.cli
  )
