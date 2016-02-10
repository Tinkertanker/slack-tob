(ns tob.slack
  (:require [aleph.http :as http]
            [taoensso.timbre :as log]
            [tob.html :as html]
            [tob.db :as db]
            [tob.utils :refer :all]))

(def ^:private TOKEN "xoxp-4246027421-18462374129-18462489777-be28887f1d")
(def ^:private TEAM-INVITE-URL "https://tinkercademy.slack.com/api/users.admin.invite")
(def ^:private GROUPS-INVITE-URL "https://slack.com/api/groups.invite")
(def ^:private POST-MESSAGE-URL "https://slack.com/api/chat.postMessage")
(def ^:private USER-LIST-URL "https://slack.com/api/users.list")
(def ^:private GROUPS-LIST-URL "https://slack.com/api/groups.list")
(def ^:private RTM-START-URL "https://slack.com/api/rtm.start")

;;;;;;;;;;;
;; Users ;;
;;;;;;;;;;;
(defrecord User [id name profile])

(defn- make-user [{:keys [id name profile]}] ;; Only grab the stuff we need
  (->User id name profile))

(defn id->user [id]
  (let [res (get-res USER-LIST-URL {:token TOKEN})
        members (val (second res))
        user (reduce (fn [b v]
                       (if (= id (:id v))
                         (reduced v)))
                     nil
                     members)]
    (make-user user)))

(defn invite-to-channel [user chan]
  (let [inv (get-res GROUPS-INVITE-URL {:token TOKEN
                                        :user (:id user)
                                        :channel chan})]
    (if (:ok inv)
      (log/info "Invite success for " (:name user))
      (log/error (str "Invite failed for " (:name user) "\n error: " (:error inv))))))

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
        (when (= "OK" (db/set-chan email channel))
          (log/info "stored email/chan pair in db"))
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
