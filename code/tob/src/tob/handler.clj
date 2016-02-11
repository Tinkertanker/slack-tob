(ns tob.handler
  (:require [bidi.ring :refer [make-handler]]
            [clojure.java.io :as io]
            [tob.resources :refer [signup-resource]]
            [yada.yada :refer [yada] :as yada]))

(def fof-handler
  (yada "404 not found."))

(def admin-handler
  (yada "admin stuff"))

(def routes
  ["/" [["" (yada "Landing Page should go here.\n")]
        ["signup/" (yada signup-resource)]
        ["admin/" admin-handler]
        ["resources/" (yada (io/file "resources/public"))]
        [true fof-handler]]])

(def handler
  (make-handler routes))
