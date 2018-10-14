(ns cljs-hangman.state
  (:require [reagent.core :as r]))

; Word(s) to guess
(defonce answer (r/atom ""))

; Characters already guessed
(defonce guessed (r/atom #{}))

; Whose go it is, :setter or :guesser
(defonce current-turn (r/atom :setter))

; Current guess (bound to the value of the guess input)
(defonce guess (r/atom ""))

(defn reset-to-new-game []
  (do
    (reset! answer "")
    (reset! guessed #{})
    (reset! current-turn :setter)
    (reset! guess "")))