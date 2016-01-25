(ns tob.handler
  (:require [bidi.ring :refer [make-handler resources-maybe]]
            [yada.yada :refer [yada] :as yada]
            [yada.resources.file-resource :refer [new-directory-resource]]
            [tob.resources :refer [signup-resource sf-resource]]
            [clojure.java.io :as io]))

(def fof-handler
  (yada "404 not found."))

(def admin-handler
  (yada "admin stuff"))

(def routes
  ["/" {"" (yada signup-resource)
        "admin/" {"" admin-handler}
        "resources/" (yada sf-resource)
        true fof-handler}])

(def handler
  (make-handler routes))
