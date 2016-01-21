(ns tob.handler)

(defn handler [req]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Hello world!"})
