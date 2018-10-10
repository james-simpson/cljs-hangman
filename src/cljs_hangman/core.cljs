(ns cljs-hangman.core
    (:require
      [reagent.core :as r]
      [cljs-hangman.logic :as logic]
      [clojure.string :as str]))

; TODO - prevent guessing an empty string
; TODO - convert answer to all lower or all upper case
; TODO - draw hangman

;; -------------------------
;; State

(defonce answer (r/atom ""))
(defonce guessed (r/atom #{}))

(defonce current-turn (r/atom :setter))
(defonce guess (r/atom ""))

;; -------------------------
;; Event handlers

(defn handler-answer-submitted [s]
  (swap! answer s))

;; -------------------------
;; Other

(defn char->placeholder
  [guessed char]
  (if (logic/has-been-guessed guessed char) char "_"))

(defn answer->placeholders
  "Display the answer with only guessed letters revealed e.g. _ o n k _ y"
  [answer guessed]
  (str/join " "
            (map (partial char->placeholder guessed) answer)))

(defn reset-to-new-game []
  (do
    (reset! answer "")
    (reset! guessed #{})
    (reset! current-turn :setter)
    (reset! guess "")))

;; -------------------------
;; Views

;; TODO: don't wrap in div?
(defn- setter-view []
  [:div
   [:h3 "Setter: enter the word(s) to guess"]
   [:input {:type         "password"
            :value        @answer
            :autoFocus    true
            :on-change    #(reset! answer (-> % .-target .-value))
            :on-key-press #(if-let [enter? (= 13 (.-charCode %))]
                             (reset! current-turn :guesser))}]
   [:button {:style {:margin-left "5px"}
             :on-click #(reset! current-turn :guesser)} "Play"]
   [:h3 {:style {:white-space "pre"}}
    (answer->placeholders @answer @guessed)]])

(defn- outcome-message [outcome]
  [:h2 {:class "outcome-message"}
   (case outcome
     :guesser-wins "Guesser wins!"
     :setter-wins (str "Setter wins! Answer was " @answer))])

(def gallows
  [:g {:id "gallows"}
   [:line {:x1 "10", :y1 "250", :x2 "150", :y2 "250"}]
   [:line {:id "door1", :x1 "150", :y1 "250", :x2 "200", :y2 "250"}]
   [:line {:id "door2", :x1 "200", :y1 "250", :x2 "250", :y2 "250"}]
   [:line {:x1 "250", :y1 "250", :x2 "300", :y2 "250"}]
   [:line {:x1 "100", :y1 "250", :x2 "100", :y2 "20"}]
   [:line {:x1 "98", :y1 "20", :x2 "202", :y2 "20"}]
   [:line {:id "rope", :x1 "200", :y1 "20", :x2 "200", :y2 "60"}]])

(def head
  [:g {:id "head"}
   [:circle {:cx "200", :cy "80", :r "20", :stroke "black", :stroke-width "4", :fill "yellow"}]
   [:g {:id "rEyes"}
    [:circle {:cx "193", :cy "80", :r "4"}]
    [:circle {:cx "207", :cy "80", :r "4"}]]
   [:g {:id "xEyes", :class "hide"}
    [:line {:x1 "190", :y1 "78", :x2 "196", :y2 "84"}]
    [:line {:x1 "204", :y1 "78", :x2 "210", :y2 "84"}]
    [:line {:x1 "190", :y1 "84", :x2 "196", :y2 "78"}]
    [:line {:x1 "204", :y1 "84", :x2 "210", :y2 "78"}]]])

(def spine [:line {:id "spine" :x1 "200", :y1 "100", :x2 "200", :y2 "150"}])

(def arm-l [:line {:id "armL", :x1 "200", :y1 "120", :x2 "170", :y2 "140"}])

(def arm-r [:line {:id "armR", :x1 "200", :y1 "120", :x2 "230", :y2 "140"}])

(def leg-l [:line {:id "legL", :x1 "200", :y1 "150", :x2 "180", :y2 "190"}])

(def leg-r [:line {:id "legR", :x1 "200", :y1 "150", :x2 "220", :y2 "190"}])

; ordered
(def hangman-parts
  [gallows head spine arm-l arm-r leg-l leg-r])

(defn- hangman-svg []
  [:svg {:height "280", :width "310"
         :class (if (>= (logic/guesses-remaining @answer @guessed) logic/no-of-guesses)
                  "hide")}
   (doall
     (map-indexed (fn [i part] (if (>= (logic/guesses-remaining @answer @guessed) (- logic/no-of-guesses i))
                                      (assoc-in part [1 :class] "hide")
                                      part))
                  hangman-parts))])

(defn- guesser-view []
  (let [outcome (logic/guess-outcome @answer @guessed)]
    [:div
     (if outcome
       (outcome-message outcome))
     [:h3 {:style {:white-space "pre"}}
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
                                (when-let [enter? (= 13 (.-charCode e))]
                                  (swap! guessed #(conj % @guess))
                                  (reset! guess "")))}]
       [:p (str "Guessed: " (str/join ", " @guessed))]
       [:p (str "Guesses left: " (logic/guesses-remaining @answer @guessed))]
       [:button {:on-click #(reset-to-new-game)}
        (if outcome "Play again" "Start again")]]
      [:div
       (hangman-svg)]
      ]]))

(defn home-page []
  [:div
   [:h2 {:class "title"} "_ h a n g m a n _"]
   [:div.game-view
    (if (= @current-turn :setter)
      (setter-view)
      (guesser-view))]])

  ;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))