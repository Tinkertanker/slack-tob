(ns tob.rtm
  (:require [taoensso.timbre :as log]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [cheshire.core :refer [generate-string parse-string]]
            [tob.slack :as slack]
            [tob.db :as db]
            [tob.utils :refer :all]
            #_[tob.dict :as dict])
  (:import [java.time Instant]
           [java.time.temporal ChronoUnit]))

(def PING_PONG 10)
(def ^:private ANN_CHAN_ID "C04780TCT")
(def ^:private TESTING_CHAN_ID "C0HMV8QF7")

(defn time-diff
  "Returns the difference (in seconds) between two Instant instances, i1 and i2"
  [i1 i2]
  (.between ChronoUnit/SECONDS i1 i2))

(defn send-msg
  "puts message into stream"
  [s message]
  (let [json (generate-string message)]
    (s/put! s json)))

(defn- ws-recon [{:keys [state handler msg-q] :as client}]
  (reset! (:status state) false)
  (reset! (:count state) 1)
  (reset! (:last-seen state) (Instant/now))
  (assoc client :msg-q (s/stream 50))
  (when-not (s/closed? @(:conn state))
    (log/error "Creating new connection...")
    (when-let [new-conn (slack/rtm-connect (slack/get-ws-url))]
      (reset! (:conn state) new-conn)
      (s/consume #(send-msg @(:conn state) %) msg-q)
      (s/consume #(-> %
                      (parse-string true)
                      (handler client)) @(:conn state)))))

(defn- ping
  [conn count]
  (send-msg conn {:id count :type "ping"}))

(defn- ping-pong-loop [{:keys [state msg-q] :as client}]
  (let [pings (s/periodically 5000 2000 #(ping @(:conn state) @(:count state)))
        pongs (s/periodically 5000 2000 #(> PING_PONG (time-diff @(:last-seen state) (Instant/now))))]
    (d/loop []
      (d/let-flow [pinged? (s/take! pings)
                   ponged? (s/take! pongs)]
        (if (and pinged? ponged?)
          (do (prn 'pinged @(:count state))
              (swap! (:count state) inc)
              (d/recur))
          (do
            (log/info "Either ping or pong failed, restarting...")
            (log/info "Closing pings..." (s/close! pings))
            (log/info "Closing pongs..." (s/close! pongs))
            (log/info "Closing connection..." (s/close! @(:conn state)))
            (log/info "Closing msg-q..." (s/close! msg-q))
            (ws-recon client)))))))

(defmulti event-handler (fn [event client] ((juxt :type :subtype) event)))

(defmethod event-handler ["hello" nil] [event {:keys [state] :as client}]
  (log/info
   "Hello received, updating state:"
   (reset! (:status state) true))
  (ping-pong-loop client))

(defmethod event-handler ["pong" nil] [event {:keys [state] :as client}]
  (let [id (:reply_to event)]
    (when (= 0 (mod id 20))
      (log/info "Still receiving pong with ID: " id)))
  (reset! (:last-seen state) (Instant/now)))

(defmethod event-handler ["reconnect_url" nil] [event {:keys [state] :as client}]
  (log/info
   "Reconnect URL received: updating state:"
   (reset! (:recon state) (:url event))))

(defmethod event-handler ["message" "channel_join"] [{:keys [user channel] :as event} {:keys [state] :as client}]
  (when (= ANN_CHAN_ID channel)
    (let [usr (slack/id->user user)
          email (get-in usr [:profile :email])]
      (if-let [chan (:channel (db/get-chan email))]
        (slack/invite-to-channel usr chan)
        (log/error (str email "not in Redis DB"))))))

#_(defmethod event-handler "message" [{:keys [text] :as event} {:keys [state msg-q] :as client}]
    (if-let [word (second (re-seq #"what is|\w+" text))]
      (s/put! msg-q {:id @(:count state)
                     :type "message"
                     :channel (:channel event)
                     :text (let [definition (dict/word-defn word)]
                             (if (:error definition)
                               (:message definition)
                               (if-let [msg (:message definition)]
                                 (str word " is " (clojure.string/lower-case msg))
                                 (str word " does not exist")
                                 )))})
      (log/info "no match for this sentence")))

#_(defmethod event-handler "user_typing" [event {:keys [state msg-q] :as client}]
    (s/put! msg-q {:id @(:count state)              
                   :type "message"
                   :channel (:channel event)
                   :text "shhhh"})
    (swap! (:count state) inc))

(defmethod event-handler :default [event {:keys [state] :as client}]
  (prn event))
