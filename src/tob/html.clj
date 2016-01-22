(ns tob.html
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer :all]))

(defn layout [& content]
  (html
   [:head
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:link {:href "https://fonts.googleapis.com/css?family=Lato|Dosis:700" :type "text/css" :rel "stylesheet"}]
    (include-css "/resources/css/main.css")]
   [:body content]))

(defn signup-page [chn]
  (layout
   [:div.container
    [:div.hero
     [:div#logo
      [:img {:src "/resources/img/logo-with-text.png"}]]
     [:div#hero-text
      [:h1 "Welcome!"]
      [:form {:method "POST"}
       [:input {:type "email" :name "email" :placeholder "Email"}]
       [:input {:type "hidden" :name "channel" :placeholder "Channel" :value chn}]
       [:button {:class "btn-large" :type "submit"}
        "Get your Slack Invite!"]]]]]))

