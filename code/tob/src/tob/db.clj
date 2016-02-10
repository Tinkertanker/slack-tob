(ns tob.db
  (:require [taoensso.carmine :as car :refer (wcar)]))

(def server1-conn {:pool {}
                   :spec {:host "redis" :port 6379}})

(defmacro wcar* [& body]
  `(car/wcar server1-conn ~@body))

(defn set-data
  "Commits email/chan-id pair into redis db
   Returns OK if success"
  [email data]
  (wcar* (car/set email data)))

(defn set-chan [email chan-id]
  (set-data email {:channel chan-id}))

(defn get-chan [email]
  (wcar* (car/get email)))
