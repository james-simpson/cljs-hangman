(ns cljs-hangman.views
  (:require
    [cljs-hangman.state :refer
     [answer guessed current-turn guess reset-to-new-game]]
    [cljs-hangman.logic :as logic]
    [clojure.string :as str]))

(defn- char->placeholder
  [guessed char]
  (if (logic/has-been-guessed guessed char) char "_"))

(defn answer->placeholders
  "Get the answer with only guessed letters revealed e.g. _ o n k _ y"
  [answer guessed]
  (map (partial char->placeholder guessed) answer))

; View shown to the setter
(defn- setter-view []
  [:div.game-view
   [:h3 "Setter: enter the word(s) to guess"]
   [:input {:type         "password"
            :value        @answer
            :autoFocus    true
            :on-change    #(reset! answer (-> % .-target .-value str/lower-case))
            :on-key-press #(if-let [enter? (= 13 (.-charCode %))]
                             (reset! current-turn :guesser))}]
   [:button {:style {:margin-left "5px"}
             :on-click #(reset! current-turn :guesser)} "Play"]
   [:h3 {:class "answer"}
    (answer->placeholders @answer @guessed)]])

(defn- outcome-message [outcome]
  [:h2 {:class "outcome-message"}
   (case outcome
     :guesser-wins "Guesser wins!"
     :setter-wins (str "Setter wins! Answer was " @answer))])

; Hangman graphic
(def gallows
  [:g {:id "gallows"}
   [:line {:x1 "10", :y1 "250", :x2 "300", :y2 "250"}]
   [:line {:x1 "100", :y1 "250", :x2 "100", :y2 "20"}]
   [:line {:x1 "98", :y1 "20", :x2 "202", :y2 "20"}]
   [:line {:id "rope", :x1 "200", :y1 "20", :x2 "200", :y2 "60"}]])

(def head
  [:g {:id "head"}
   [:circle {:cx "200", :cy "80", :r "20", :stroke "black", :stroke-width "4", :fill "yellow"}]
   [:g {:id "eyes"}
    [:circle {:cx "193", :cy "80", :r "4"}]
    [:circle {:cx "207", :cy "80", :r "4"}]]])

(def spine [:line {:id "spine" :x1 "200", :y1 "100", :x2 "200", :y2 "150"}])
(def arm-l [:line {:id "armL", :x1 "200", :y1 "120", :x2 "170", :y2 "140"}])
(def arm-r [:line {:id "armR", :x1 "200", :y1 "120", :x2 "230", :y2 "140"}])
(def leg-l [:line {:id "legL", :x1 "200", :y1 "150", :x2 "180", :y2 "190"}])
(def leg-r [:line {:id "legR", :x1 "200", :y1 "150", :x2 "220", :y2 "190"}])

(def hangman-parts
  [gallows head spine arm-l arm-r leg-l leg-r])

(defn hangman-svg []
  (let [incorrect-count (count (logic/incorrect-guesses @answer @guessed))
        parts-to-show (take incorrect-count hangman-parts)]
    (if (> incorrect-count 0)
      [:svg {:height "280", :width "310"}
       (for [part parts-to-show]
         ^{:key (:id (second part))} part)])))

; View shown to the guesser
(defn- guesser-view []
  (let [outcome (logic/guess-outcome @answer @guessed)]
    [:div.game-view
     (if outcome
       (outcome-message outcome))
     [:h3 {:class "answer"}
      (answer->placeholders @answer @guessed)]
     [:div {:class "guesser-view"}
      [:div
       [:input {:type         "text"
                :value        @guess
                :autoFocus    true
                :maxLength    1
                :disabled     (boolean outcome)
                :on-change    #(reset! guess (-> % .-target .-value))
                :on-key-press (fn [e]
                                (let [enter? (= 13 (.-charCode e))]
                                  (when (and enter? (logic/valid-guess? @guess))
                                    (swap! guessed #(conj % (str/lower-case @guess)))
                                    (reset! guess ""))))}]
       [:p (str "Guessed: " (str/join ", " @guessed))]
       [:p (str "Guesses left: " (logic/guesses-remaining @answer @guessed))]
       [:button {:on-click #(reset-to-new-game)}
        (if outcome "Play again" "Start again")]]
      [:div
       (hangman-svg)]]]))

; Home
(defn home-page []
  [:div
   [:h2 {:class "title"} "_ h a n g m a n _"]
   (if (= @current-turn :setter)
     (setter-view)
     (guesser-view))])