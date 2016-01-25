(ns tob.components
  (:require [aleph.http :refer [start-server]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [taoensso.timbre.appenders.core :as appenders]))

(log/merge-config!
 {:appenders {:spit (appenders/spit-appender {:fname "log.txt"})}})

(defrecord WebServer [port server handler]
  component/Lifecycle
  (start [component]
    (let [server (start-server handler {:port port :join? false})]
      (log/info (str "Webserver started on port " port))
      (assoc component :server server)))
  (stop [component]
    (when server
      (.close server)
      (log/info (str "Webserver stopping"))
      component)))

(defn new-web-server [port handler]
  (map->WebServer {:port port :handler handler}))



