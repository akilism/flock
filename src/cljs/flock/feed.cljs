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
  [{:keys [title unread] :as feed}]
  (str title))

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
  "Map over the feeds and assoc the new articles collection to the matching feed."
  [feeds {:keys [id articles]}]
  ;(println feeds)
  (map (fn check-feed [feed]
         (cond
           (:feeds feed) (assoc feed :feeds (map #(check-feed %) (:feeds feed)))
           (= id (:id feed)) (assoc feed :articles articles)
           :else feed)) feeds))

(defn get-feed-by-id
  "Gets the feed from the supplied sequence of feeds by the supplied id."
  [id feeds]
  (let [matching-feeds (mapcat (fn check-feed [feed]
         (cond
           (:feeds feed) (map #(check-feed %) (:feeds feed))
           (= id (:id feed)) feed)) feeds)]
    (first (filter seq matching-feeds))))

(defn get-active-feed
  "Find and return the current active feed."
  [active-feed feeds]
  (first (filter #(= active-feed (:id %)) feeds)))

(defn toggle-group
  [evt group]
  (let [target (.-target evt)
        class-list (.-classList target)]
    (.toggle class-list "open")
    (.toggle class-list "closed")
;    (cond
;      (.contains class-list "open")
;      (.contains class-list "closed"))
    (println class-list)))

(defmulti feed-view
  (fn [data _]
    (if (nil? (:feeds data)) :feed :group)))

; A feed item
(defmethod feed-view :feed
  [feed owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [get-articles]}]
      ; (println "feed:" feed)
      (dom/li #js {:className "feed"}
        (dom/a #js {:onClick  #(put! get-articles (:id feed))} (feed-display feed))
        (om/build feed-unread (:unread feed))))))

; A feed group
(defmethod feed-view :group
  [group owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [get-articles]}]
      ; (println "feed group:" group)
      (dom/div nil
        (dom/p #js {:className "group open" :onClick #(toggle-group % (:title group))} (:title group))
        (apply dom/ul #js {:className "feed-list"}
            (om/build-all feed-view (:feeds group)
              {:init-state {:get-articles get-articles}}))))))


(defn get-height []
  (str (- (parseInt (.-availHeight js/screen) 10) 125) "px"))

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
          (let [feed-id (<! get-articles)]
            (go
              (let [articles (<! (handler/get-feed feed-id))]
                (om/transact! data :active-feed (fn [_] (:id articles)))
                (om/transact! data :feeds #(set-articles % articles))))
            (recur))))))
    om/IRenderState
    (render-state [this {:keys [get-articles]}]
      (dom/div #js {:className "content"}
        (dom/div #js {:className "feeds" :style #js {:height (get-height)}}
          (apply dom/ul #js {:className "group-list"}
            (om/build-all feed-view (:feeds data)
              {:init-state {:get-articles get-articles}})))
        (om/build article/article-list-view (get-feed-by-id  (:active-feed data) (:feeds data)))))))

;:height
