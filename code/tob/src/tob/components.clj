(ns tob.components
  (:require [aleph.http :refer [start-server]]
            [tob.slack :as slack]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [cheshire.core :refer [generate-string parse-string]]
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

(defn send-msg [conn message]
  (let [json (generate-string message)]
    (s/put! conn json)))

(defn- ping [conn id]
  (future (d/loop []
            (Thread/sleep 5000)
            (send-msg conn {:id id :type "ping"})
            (d/recur))))

(defrecord WSClient [id msg-count status handler conn]
  component/Lifecycle
  (start [component]
    (let [msg-count (atom 0)
          ws-url (slack/get-ws-url)
          conn (slack/rtm-connect ws-url)
          status (atom true)]
      (log/info "Starting WS")
      (s/consume (fn [msg]
                   (do (swap! msg-count inc)
                       (-> msg
                           (parse-string true)
                           handler))
                   conn))
      #_(ping conn)
      (assoc component
             :status true
             :msg-count msg-count
             :conn conn)))
  
  (stop [component]
    (when status
      (log/info "Closing WS")
      (s/close! conn)
      (assoc component
             :status false
             :msg-count (atom 0)
             :url nil
             :conn nil))))

(defn new-ws-client [id handler]
  (map->WSClient {:id id :handler handler}))
