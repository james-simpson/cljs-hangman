;;;
; Game logic
;;;

(ns cljs-hangman.logic
  (:require
    [clojure.string :as str]))

(def no-of-guesses 7)

(defn has-been-guessed
  [guessed char]
  (or (contains? guessed char) (= char \space)))

(defn answer-guessed?
  [answer guessed]
  (every? (partial has-been-guessed guessed) answer))

(defn valid-guess?
  [guess]
  (not= guess ""))

(defn incorrect-guesses
  [answer guessed]
  (remove (partial contains? (set answer)) guessed))

(defn guesses-remaining
  [answer guessed]
  (- no-of-guesses
     (count (incorrect-guesses answer guessed))))

(defn guess-outcome
  [answer guessed]
  (if (answer-guessed? answer guessed)
    :guesser-wins
    (if (= (guesses-remaining answer guessed) 0)
      :setter-wins)))

;;;
; Displaying game state
;;;
(defn char->placeholder
  [guessed char]
  (if (has-been-guessed guessed char)
    char
    "_"))

(defn answer->placeholders
  "Display the answer with only guessed letters revealed e.g. _ o n k _ y"
  [answer guessed]
  (str/join " "
            (map (partial char->placeholder guessed) answer)))
