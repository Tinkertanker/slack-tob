(ns tob.core
  (:gen-class)
  (:require [tob.systems :refer [prod-system]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]))

(def sys nil)

(defn -main
  "Run application in production mode."
  []
  (alter-var-root #'sys (constantly (prod-system)))
  (alter-var-root #'sys component/start))
