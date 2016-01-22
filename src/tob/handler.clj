(ns tob.handler
  (:require [bidi.ring :refer [make-handler resources-maybe]]
            [yada.yada :refer [yada] :as yada]
            [yada.resource :refer [resource]]
            [schema.core :as s]
            [tob.slack :as slack]
            [tob.html :as html]))

(def signup-resource
  (yada (resource
         {:description "Slack Invites"
          :methods {:get {:parameters {:query {(s/required-key :channel) String}}
                          :produces [{:media-type #{"text/html"}
                                      :charset "UTF-8"}]
                          :response (fn [ctx]
                                      (let [c (get-in ctx [:parameters :query :channel])]
                                        (case (yada/content-type ctx)
                                          "text/html" (html/signup-page c))))}
                    :post {:parameters {:form {:channel String :email String}}
                           :consumes [{:media-type #{"application/x-www-form-urlencoded"}
                                       :charset "UTF-8"}]
                           :response (fn [ctx]
                                       (let [email (get-in ctx [:parameters :form :email])
                                             channel (get-in ctx [:parameters :form :channel])]
                                         "text/html" (slack/process-signup email channel)))}}})))

(def fof-handler
  (yada "404 not found."))

(def admin-handler
  (yada "admin stuff"))

(def routes
  ["/" {"" signup-resource
        "admin/" {"" admin-handler}
        "resources/" (resources-maybe {:prefix "public/"})
        true fof-handler}])

(def handler
  (make-handler routes))
