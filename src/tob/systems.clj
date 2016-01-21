(ns tob.systems
  (:require [com.stuartsierra.component :as component]
            [tob.components :refer :all]
            [tob.handler :refer [handler]]))

(defn dev-system
  []
  (component/system-map
   :web (new-web-server 3000 handler)))

(defn prod-system
  []
  (component/system-map
   :web (new-web-server 8080 handler)))
