(ns ^:figwheel-always flock.front-core
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [flock.front-feed :as feed]))

(enable-console-print!)

(println "console is king.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:active-feed 0
                          :active-name " "
                          :feeds [{:name "Cool Tools" :id 1 :url "http://kk.org/cooltools/feed" :unread 10}
                                  {:name "Ridge" :id 2 :url "http://one9638.blog79.fc2.com/?xml" :unread 40}
                                  {:name "Jason Kottke" :id 3 :url "http://feeds.kottke.org/main" :unread 30}
                                  {:name "Kewl Tools" :id 4 :url "http://kk.org/cooltools/feed" :unread 20}]}))

(om/root
  feed/feed-list-view
  app-state
  {:target (. js/document (getElementById "app"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(defn main [])
