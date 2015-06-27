(ns ^:figwheel-always flock.front-feed
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [flock.front-article :as article]
              [flock.front-handler :as handler]
              [cljs.core.async :refer [<! put! chan] :as async]))

(defn parseInt [str-n x]
  (.parseInt js/Number str-n x))

;;TODO: move this to app-state
(def high-count 25)

(defn feed-display
  [{:keys [name unread] :as feed}]
  (str name))

(defn badge-classer
  [high]
  (if high "badge badge-unread high" "badge badge-unread"))

(defn is-high? [unread-count]
  (<= high-count unread-count))

(defn badge-display
  [unread]
  (let [high (is-high? unread)]
    (dom/span #js {:className (badge-classer high)} unread)))

(defn feed-unread
  [unread owner]
  (reify
    om/IRender
    (render [this]
      (badge-display unread))))

(defn set-articles
  "Loop over the feeds and assoc the new articles collection to the matching feed."
  [feeds {:keys [id articles]}]
  (loop [fs feeds acc []]
    (cond
      (= 0 (count fs)) (vec acc)
      (= (parseInt id 10) (:id (first fs)))
        (recur (rest fs) (conj acc (assoc (first fs) :articles articles)))
      :else (recur (rest fs) (conj acc (first fs))))))

(defn get-active-feed
  "Find and return the current active feed."
  [active-feed feeds]
  (first (filter #(= active-feed (parseInt (:id %) 10)) feeds)))

(defn feed-view
  "A feed item"
  [feed owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [get-articles]}]
      (dom/li nil
        (dom/a #js {:onClick  #(put! get-articles @feed)} (feed-display feed))
        (om/build feed-unread (:unread feed))))))

(defn feed-list-view
  "The feed list"
  [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:get-articles (chan)})
    om/IWillMount
    (will-mount [_]
      (let [get-articles (om/get-state owner :get-articles)]
        (go (loop []
          (let [feed (<! get-articles)]
            (go
              (let [articles (<! (handler/get-feed (:id feed)))]
                (om/transact! data :active-feed (fn [_] (parseInt (:id articles) 10)))
                (om/transact! data :feeds #(set-articles % articles))
                ))
            (recur))))))
    om/IRenderState
    (render-state [this {:keys [get-articles]}]
      (dom/div nil
        (dom/div nil "feeds")
        (apply dom/ul #js {:className "feed-list"}
          (om/build-all feed-view (:feeds data)
            {:init-state {:get-articles get-articles}}))
        (om/build article/article-list-view (get-active-feed (:active-feed data) (:feeds data)))))))
