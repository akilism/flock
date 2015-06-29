(ns ^:figwheel-always flock.front-core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [flock.front-feed :as feed]
              [flock.front-handler :as handler]
              [cljs.core.async :refer [<!] :as async]))

(enable-console-print!)

(println "console is king.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:active-feed 0
                          :active-name ""
                          :feeds []}))

(go
  (let [{:keys [feeds]} (<! (handler/get-feeds))]
    (swap! app-state assoc :feeds feeds)
    (om/root
      feed/feed-list-view
      app-state
      {:target (. js/document (getElementById "app"))})))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(defn main [])
