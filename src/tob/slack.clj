(ns tob.slack
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [cheshire.core :refer [parse-string]]
            [taoensso.timbre :as log]))

(def ^:private TOKEN "xoxp-4246027421-18462374129-18462489777-be28887f1d")
(def ^:private GROUP-INVITE-URL "https://slack.com/api/groups.invite")
(def ^:private POST-MESSAGE-URL "https://slack.com/api/chat.postMessage")
(def ^:private USER-LIST-URL "https://slack.com/api/users.list")
(def ^:private GROUPS-LIST-URL "https://slack.com/api/groups.list")
(def ^:private RTM-START-URL "https://slack.com/api/rtm.start")

(defn- get-response
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

(defn- get-ws-url
  "Sends GET req to RTM URL"
  [token]
  (let [res (get-response RTM-START-URL {:token token})]
    (if (:ok res)
      (:url res)
      (log/error (str ";; Error getting WS url response received: " res)))))
