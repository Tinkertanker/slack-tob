(ns tob.rtm)

(defmulti event-handler (fn [event status] (:type event)))

(defmethod event-handler "reconnect_url" [event status]
  (reset! status false)
  (prn "got reconnect-url, changing status: " status))

(defmethod event-handler :default [event status]
  (prn event))
