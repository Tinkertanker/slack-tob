(ns tob.resources
  (:require [yada.yada :refer [yada] :as yada]
            [yada.resource :refer [resource]]
            [schema.core :as s]
            [tob.slack :as slack]
            [tob.html :as html]
            [bidi.ring :refer [make-handler resources-maybe]]
            [yada.yada :refer [yada] :as yada]
            [yada.resources.file-resource :refer [new-directory-resource]]
            [tob.resources :refer [signup-resource]]
            [clojure.java.io :as io]))

(def signup-resource
  (resource
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
                                   "text/html" (slack/process-signup email channel)))}}}))

(def sf-resource
  (new-directory-resource
   (io/file (io/resource "public"))
   {}))
