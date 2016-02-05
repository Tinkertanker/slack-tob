(ns tob.utils
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [manifold.deferred :as d]
            [cheshire.core :refer [parse-string]]))

(defn get-res
  "Sends asynchronous GET request with params, returns BODY"
  ([url]
   (-> (d/chain (http/get url {:connection-timeout 5000})
                :body
                bs/to-string)
       (d/catch Exception (fn [e]
                            (str "{\"error\":\"" "Timed out after 5s" "\"}")))
       deref
       (parse-string true)))
  ([url params]
   (-> @(http/get url {:query-params params})
       :body
       bs/to-string
       (parse-string true))))

(defn lookup2 [url]
  (-> @(http/get url)
      :body
      bs/to-string
      prn))

(defn send-req
  "Sends asynchronous POST request with params, returns BODY"
  [url params]
  (-> @(http/post url {:form-params params})
      :body
      bs/to-string
      (parse-string true)))
