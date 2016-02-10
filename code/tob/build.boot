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
                 [hiccup "1.0.5"]

                 ;;DB
                 [com.taoensso/carmine "2.12.2"]

                 ;;Logging
                 [com.taoensso/timbre "4.2.1"]

                 ;;Assets
                 [mathias/boot-sassc "0.1.5"]

                 ;;Packaging
                 [adzerk/boot-ubermain "1.0.0-SNAPSHOT"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]
                 ])

(require '[tob.core :refer :all])
(require '[tob.systems :refer [dev-system prod-system]])
(require '[system.boot :refer [system run]])
(require '[mathias.boot-sassc :refer [sass]])
(require '[adzerk.boot-ubermain :refer [ubermain]])
(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.0.1")
(bootlaces! +version+)

(deftask testing
  "Profile setup for running tests."
  []
  (set-env! :source-paths #(conj % "test"))
  identity)

(deftask deps
  "Fetches dependencies."
  [])

#_(deftask prod []
    (comp 
     (run :main-namespace "tob.core")
     (wait)))

(deftask scss-profile []
  (set-env! :resource-paths #{"sass"})
  identity)

(deftask scss []
  (comp
   (scss-profile)
   (sass :sass-file "main.scss")
   (sift :move {#"main.css" "../resources/public/css/main.css"})))

(deftask dev []
  (comp
   (watch :verbose true)
   #_(scss)
   (repl :server true :port 1337 :bind "0.0.0.0")
   (system :sys #'dev-system :auto-start true :hot-reload true)
   ))

(deftask pkg []
  (comp
   (ubermain :main-var 'tob.core/-main)))
