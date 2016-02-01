(ns tob.rtm)

(defmulti event-handler (fn [event] (:type event)))

(defmethod event-handler "reconnect_url" [event]
  (prn "got reconnect-url"))

(defmethod event-handler :default [event]
  (prn event))
