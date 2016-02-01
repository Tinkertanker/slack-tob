(ns tob.systems
  (:require [com.stuartsierra.component :as component]
            [tob.components :refer :all]
            [tob.handler :refer [handler]]))

(defmulti event-handler (fn [event] (:type event)))

(defmethod event-handler "reconnect_url" [event]
  (prn "got reconnect-url"))

(defmethod event-handler :default [event]
  (prn event))

(defn dev-system
  []
  (component/system-map
   :web (new-web-server 3000 handler)
   :ws (new-ws-client (rand-int 100) event-handler)))

(defn prod-system
  []
  (component/system-map
   :web (new-web-server 8080 handler)))
