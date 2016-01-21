(set-env!
 :source-paths #{"src"}
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [org.danielsz/system "0.2.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [aleph "0.4.1-beta3"]
                 [cheshire "5.5.0"]
                 [bidi "1.25.0"]
                 [yada "1.1.0-SNAPSHOT"]
                 [org.clojure/core.cache "0.6.4"]

                 ;;Logging
                 [com.taoensso/timbre "4.2.1"]])

(require '[tob.core :refer :all])
(require '[tob.systems :refer [dev-system prod-system]])
(require '[system.boot :refer [system run]])

(deftask testing
  "Profile setup for running tests."
  []
  (set-env! :source-paths #(conj % "test"))
  identity)

(deftask deps
  "Fetches dependencies."
  [])

(deftask dev []
  (comp
   (watch :verbose true)
   (repl :server true)
   (system :sys #'dev-system :auto-start true :hot-reload true)
   ))

(deftask prod []
  (comp 
   (run :main-namespace "tob.core")
   (wait)))
