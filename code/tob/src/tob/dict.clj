(ns tob.dict
  (:require [tob.utils :refer :all]))

(defn word-url
  "Takes a word, formulates URL"
  [word]
  (str "http://api.wordnik.com:80/v4/word.json/" word "/definitions?limit=1&includeRelated=true&sourceDictionaries=all&useCanonical=false&includeTags=false&api_key=a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5"))

(defn word-defn
  "Takes word, returns defn or nil if not found"
  [word]
  (let [res (-> (word-url word)
                get-res)]
    (if (:error res)
      {:error true
       :message "Timed out. Please try again later."}
      (if-let [word-def (-> res first :text)]
        {:error false :message word-def}
        {:error false :message nil})))
  )
