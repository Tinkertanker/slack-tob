(ns tob.components
  (:require [aleph.http :refer [start-server]]
            [tob.slack :as slack]
            [tob.rtm :refer [send-msg]]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [cheshire.core :refer [parse-string]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [taoensso.timbre.appenders.core :as appenders])
  (:import [java.time Instant]))

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

(defrecord WSClient [state handler msg-q]
  component/Lifecycle
  (start [component]
    (let [ws-url (slack/get-ws-url)
          conn (slack/rtm-connect ws-url)
          msg-q (s/stream 50)
          state {:status (atom false)
                 :last-seen (atom (Instant/now))
                 :count (atom 1)
                 :recon (atom nil)
                 :conn (atom conn)}
          new-comp (assoc component :state state :msg-q msg-q)]
      (log/info "Starting WS")
      (s/consume #(send-msg @(:conn state) %) msg-q)
      (s/consume
       (fn [msg]
         (-> msg
             (parse-string true)
             (handler new-comp)))
       @(:conn state))
      new-comp))
  (stop [component]
    (when @(:status state)
      (log/info "Commencing WebSocket shutdown:")
      (log/info "Reset status: " (reset! (:status state) false))
      (log/info "Closing WS")
      (s/close! @(:conn state))
      (s/close! msg-q)
      (assoc component
             :state nil))))

(defn new-ws-client [handler]
  (map->WSClient {:handler handler}))
