(ns ^:figwheel-no-load cljs-hangman.dev
  (:require
    [cljs-hangman.core :as core]
    [devtools.core :as devtools]))


(enable-console-print!)

(devtools/install!)

(core/init!)
