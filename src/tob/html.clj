(ns tob.html
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer :all]))

;;;;;;;;;;;;
;; Layout ;;
;;;;;;;;;;;;

(defn layout [& content]
  (html
   [:head
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:link {:href "https://fonts.googleapis.com/css?family=Lato|Dosis:700" :type "text/css" :rel "stylesheet"}]
    (include-css "/resources/css/main.css")]
   [:body content]))

;;;;;;;;;;;;
;; Errors ;;
;;;;;;;;;;;;
(defn- error-page [text]
  (layout
   [:div.container
    [:div.hero
     [:div#logo
      [:img {:src "/resources/img/logo-cry.png"}]]
     [:div#hero-text
      [:h1 "Oops!"]
      [:p text]
      [:button {:onClick "javascript:history.back()"} "Go Back"]]]]))

(def error-already-invited-page (error-page "You're already invited. Please check your email again."))

(def error-already-in-team-page (error-page "You're already in Slack. You can sign in <a href=\"http://tk.sg/slack\">here</a> with your credentials."))

(def error-403-page (error-page "Error 403. Permission Denied."))

(def error-404-page (error-page "Error 404. Page not found."))

;;;;;;;;;;;;;;;;;;
;; Slack Signup ;;
;;;;;;;;;;;;;;;;;;

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

(def signup-success-page
  (layout
   [:body
    [:div.container
     [:div.hero
      [:div#logo
       [:img {:src "/resources/img/logo-with-text.png"}]]
      [:div#hero-text.flow-text
       [:h1 "Success!"]
       [:p "Check your email to verify your invite."]]]]]))
