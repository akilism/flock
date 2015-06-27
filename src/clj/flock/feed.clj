(ns flock.feed
  (:require [flock.data-access :as data-access]))

(defn fetch-feeds [req]
  {:feeds [{:id 1, :name "Cool Tools", :url "http://kk.org/cooltools/feed"}
           {:id 2, :name "JJJJound" ,:url "http://jjjjound.com/?feed=rss2"}]})

(defn fetch-feed-data [str-id req]
  (println (str "Getting feed data for id: " str-id))
  (let [id (bigint str-id)]
    (cond
    (= 1 id) {:id id :articles [1 11 111 1111 11111]}
    (= 2 id) {:id id :articles [2 22 222 2222 22222]}
    (= 3 id) {:id id :articles [3 33 333 3333 33333]}
    (= 4 id) {:id id :articles [4 44 444 4444 44444]})))
