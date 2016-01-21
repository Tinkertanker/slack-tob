(ns tob.slack
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [cheshire.core :refer [parse-string]]))

(def ^:private GROUP-INVITE-URL "https://slack.com/api/groups.invite")
(def ^:private POST-MESSAGE-URL "https://slack.com/api/chat.postMessage")
(def ^:private USER-LIST-URL "https://slack.com/api/users.list")
(def ^:private GROUPS-LIST-URL "https://slack.com/api/groups.list")

(defn- get-response
  "Sends asynchronous GET request with params, returns BODY"
  ([url]
   (-> @(http/get url)
       :body
       bs/to-string))
  ([url params]
   (-> @(http/get url {:query-params params})
       :body
       bs/to-string)))
