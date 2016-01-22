(ns tob.handler
  (:require [bidi.ring :refer [make-handler resources-maybe]]
            [yada.yada :refer [yada] :as yada]))

(def fof-handler
  (yada "404 not found."))

(def admin-handler
  (yada "admin stuff"))

(def routes
  ["/" {"" (yada signup-resource)
        "admin/" {"" admin-handler}
        "resources/" (resources-maybe {:prefix "public/"})
        true fof-handler}])

(def handler
  (make-handler routes))
