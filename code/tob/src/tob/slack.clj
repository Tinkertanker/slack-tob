(ns tob.slack
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [cheshire.core :refer [parse-string]]
            [taoensso.timbre :as log]
            [tob.html :as html]))

(def ^:private TOKEN "xoxp-4246027421-18462374129-18462489777-be28887f1d")
(def ^:private TEAM-INVITE-URL "https://tinkercademy.slack.com/api/users.admin.invite")
(def ^:private GROUP-INVITE-URL "https://slack.com/api/groups.invite")
(def ^:private POST-MESSAGE-URL "https://slack.com/api/chat.postMessage")
(def ^:private USER-LIST-URL "https://slack.com/api/users.list")
(def ^:private GROUPS-LIST-URL "https://slack.com/api/groups.list")
(def ^:private RTM-START-URL "https://slack.com/api/rtm.start")

;;;;;;;;;;;;;;;;;
;; Slack Utils ;;
;;;;;;;;;;;;;;;;;
(defn- get-res
  "Sends asynchronous GET request with params, returns BODY"
  ([url]
   (-> @(http/get url)
       :body
       bs/to-string
       (parse-string true)))
  ([url params]
   (-> @(http/get url {:query-params params})
       :body
       bs/to-string
       (parse-string true))))

(defn- send-req
  "Sends asynchronous POST request with params, returns BODY"
  [url params]
  (-> @(http/post url {:form-params params})
      :body
      bs/to-string
      (parse-string true)))

;;;;;;;;;;;;;;;;;;
;; Slack Invite ;;
;;;;;;;;;;;;;;;;;;
(defn- dispatch-error [err]
  (condp = err
    "already_invited" html/error-already-invited-page
    "already_in_team" html/error-already-in-team-page))

(defn process-signup
  "Sends invite to email, adds email, channel to table if invite succeeded"
  [email channel]
  (let [res (send-req
             TEAM-INVITE-URL
             {:token TOKEN
              :email email})]
    (if (:ok res)
      (do
        (log/info (str "Slack signup sent to " email))
        html/signup-success-page)
      (dispatch-error (:error res)))))

;;;;;;;;;;;;;;;;
;; Websockets ;;
;;;;;;;;;;;;;;;;
(defn get-ws-url
  "Sends GET req to RTM URL"
  []
  (let [res (get-res RTM-START-URL {:token TOKEN})]
    (if (:ok res)
      (:url res)
      (log/error (str ";; Error getting WS url response received: " res)))))


(defn rtm-connect [url]
  @(http/websocket-client url))
