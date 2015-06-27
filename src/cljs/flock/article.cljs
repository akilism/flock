(ns ^:figwheel-always flock.front-article
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [flock.front-handler :as handler]
              [cljs.core.async :refer [<! put! chan] :as async]))

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
      ;(dom/a nil (:title article))
      ;(dom/p nil (:text article))
      ;(om/build nil list-nav (article))
      (dom/span nil article))))

(defn article-list-item
  [article owner]
  (reify
    om/IRender
    (render [this]
      (println "article-list-item: " article)
      (dom/li nil
        (om/build article-display article)))))

(defn article-list-view
  [data owner]
  (reify
    om/IRender
    (render [this]
      (println "article-list-view: " data)
      (dom/div nil
        (dom/h2 #js {:className "feed-title"} (:name data))
        (apply dom/ul #js {:className "article-list"}
          (om/build-all article-list-item (:articles data)))))))
