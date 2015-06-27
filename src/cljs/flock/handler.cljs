(ns ^:figwheel-always flock.front-handler
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!] :as async]))

;; TODO: Dry this up.
(defn build-api-url
  [type id]
  (cond
    (= :article type) (str "/api/articles/" id)
    (= :feed type) (str "/api/feeds/" id)
    :else "/api/feeds"))

(defn build-url
  [type id]
  (cond
    (= :article type) (str "/articles/" id)
    (= :feed type) (str "/feeds/" id)
    :else "/"))

(defn get-feed
  [id]
    (go (let [url (build-api-url :feed id)
              response (<! (http/get url))
              body (:body response)]
          (:body response))))

(defn get-article
  [id]
    (go (let [url (build-api-url :article id)
              response (<! (http/get url))
              body (:body response)]
          (:body response))))
