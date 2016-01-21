(ns tob.handler
  (:require [bidi.ring :refer [make-handler]]
            [yada.yada :refer [yada] :as yada]))

(def index-handler
  (yada (atom "Hello World!")))

(def fourohfour-handler
  (yada "404 not found."))

(def admin-handler
  (yada "admin stuff"))

(def routes
  ["/" {"" index-handler
        "admin/" {"" admin-handler}
        true fourohfour-handler}])

(def handler
  (make-handler routes))
