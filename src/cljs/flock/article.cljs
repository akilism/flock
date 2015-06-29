(ns ^:figwheel-always flock.front-article
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [flock.front-handler :as handler]))

(defn list-nav
  [article owner]
  (reify
    om/IRender
    (render [this]
      (dom/ul #js {:className "article-list-nav"}
        (dom/li nil "starred")
        (dom/li nil "unread")
        (dom/li nil "date")
      ))))

(defn article-display
  [article owner]
  (reify
    om/IRender
    (render [this]
      (dom/li #js {:className "article"}
        (dom/p #js {:className "article-title"} (:title article))
        (dom/p #js {:className "article-pub-date"} (:pubDate article))
        (dom/p #js {:className "article-creator"} (:creator article))
        (dom/p #js {:className "article-category"} (:category article))
        (dom/p #js {:className "article-description"} (:description article))
      ;(om/build nil list-nav (article))
      ;(dom/span nil article)
      ))))

(defn article-list-item
  [article owner]
  (reify
    om/IRender
    (render [this]
      ;(println "article-list-item: " article)
      (om/build article-display article))))

(defn article-list-view
  [data owner]
  (reify
    om/IRender
    (render [this]
;;      (println "article-list-view: " data)
      (dom/div #js {:className "articles"}
        (dom/h2 #js {:className "feed-title"} (:name data))
        (apply dom/ul #js {:className "article-list"}
          (om/build-all article-list-item (:articles data)))))))
