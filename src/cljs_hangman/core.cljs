(ns cljs-hangman.core
    (:require
      [reagent.core :as r]
      [cljs-hangman.views :as views]))

; Initialize app
(defn mount-root []
  (r/render [views/home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))