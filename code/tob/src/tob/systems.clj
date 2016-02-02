(ns tob.systems
  (:require [com.stuartsierra.component :as component]
            [tob.components :refer :all]
            [tob.handler :refer [handler]]
            [tob.rtm :refer [event-handler]]))

(defn dev-system
  []
  (component/system-map
   :web (new-web-server 3000 handler)
   :ws (new-ws-client event-handler)))

(defn prod-system
  []
  (component/system-map
   :web (new-web-server 8080 handler)
   :ws (new-ws-client event-handler)))
