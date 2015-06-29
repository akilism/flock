(ns flock.feed
  (:require [flock.data-access :as data-access]
            [clojure.pprint :as pp]
            [clojure.core.async :refer [<! go] :as async]))

(defn get-item
  [xml-data]
  (:content (nth (:content xml-data) 1)))

; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
; Functions to parse opml files for insertion into the database.
; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defmulti get-opml-item
  (fn [type _] type))

(defn extract-opml-feeds
  [feed-data]
  (map #(get-opml-item (:type (:attrs %)) %) feed-data))

(defmethod get-opml-item nil [_ data]
  (let [title (:title (:attrs data))
        text (:text (:attrs data))
        feeds (extract-opml-feeds (:content data))]
    {:title title :text text :feeds feeds :type :group}))

(defmethod get-opml-item "rss" [_ data]
  (assoc (:attrs data) :type :feed))

(defn get-opml-data
  [xml-data]
  (:content (first (filter #(= :body (:tag %)) (:content xml-data)))))

(defn parse-feeds
  [xml-data]
  (extract-opml-feeds (get-opml-data xml-data)))

(defn fetch-feeds-file [req]
  (let [xml-data (data-access/get-feeds-file "./subscriptions.opml")]
    ; (pp/pprint (parse-feeds xml-data))
    (parse-feeds xml-data)))

; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
; opml interfacing with the database. only store feeds not groups in feeds database.
; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
(defn get-only-feeds
  [acc item]
  (let [type (:type item)]
    (cond
      (= :group type) (reduce get-only-feeds acc (:feeds item))
      (= :feed type) (conj acc item))))

(defn get-only-feed-names
  [acc item]
  (let [type (:type item)]
    (cond
      (= :group type) (reduce get-only-feeds acc (:feeds item))
      (= :feed type) (conj acc item))))

(defn get-id
  [feed transform-feeds]
  (:_id (first (filter #(= (:xmlUrl feed) (:feedUrl %)) transform-feeds))))

; need to read a type out of the request to see if we should read
; a supplied opml file or goto the database to fetch the users feeds.
; think about moving this to it's own function.
(defn fetch-feeds [req]
  (let [feeds (fetch-feeds-file req)
        ; transform-feeds (data-access/create-feeds (reduce get-only-feeds '() feeds))
        transform-feeds (data-access/fetch-feeds (reduce get-only-feed-names '() feeds))
        ]
    (println transform-feeds)
    {:feeds (map (fn add-id [feed]
                   (cond
                     (= :group (:type feed)) (assoc feed :feeds (map add-id (:feeds feed)))
                     (= :feed (:type feed)) (assoc feed :id (get-id feed transform-feeds) :feedUrl (:xmlUrl feed)))
                   ) feeds)}))


(defn fetch-feed-data [str-id req]
  (println (str "Getting feed data for id: " str-id))
  (let [id (bigint str-id)]
    (cond
    (= 1 id) {:id id :articles [1 11 111 1111 11111]}
    (= 2 id) {:id id :articles [2 22 222 2222 22222]}
    (= 3 id) {:id id :articles [3 33 333 3333 33333]}
    (= 4 id) {:id id :articles [4 44 444 4444 44444]}
    (= 5 id) {:id id :articles [5 55 555 5555 55555]})))

(defn get-feed-url
  [id]
  (println (str "Getting feed data for id: " id))
  (:feedUrl (first (filter #(= id (:id %)) (:feeds (fetch-feeds ""))))))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Generic Feed Traversal/Transformation Functions
;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
(defn get-element-val
  "Takes a predicate function and an XML Tree and filters the tree for elements that satisfy the predicate."
  [pred xml-tree]
  (filter pred xml-tree))

(defn get-feed-data-by-tags
  "Take a list of feed tags and returns a function that takes an XML document tree. That function will filter the XML Tree for the tags specified."
  [tags]
  (fn [xml-tree]
    (get-element-val (fn [x] (contains? tags (:tag x))) xml-tree)))

(defn flatten-details
  "Flattens a sequence of {tag content} paris into a single map"
  [transform-details {tag :tag content :content}]
  (cond
    (nil? tag) transform-details
    :else (assoc transform-details tag (first content))))


;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Feed Type Specific Functions
;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
(defn get-rss-feed-content [xml-tree]
  (:content (first (:content xml-tree))))

(defn get-rdf-feed-content [xml-tree]
  (:content xml-tree))

(defn get-atom-feed-content [xml-tree]
  (:content xml-tree))

(defn get-rss-article-content [xml-tree]
  (get-element-val (fn [x] (= :item (:tag x))) (get-rss-feed-content xml-tree)))

(defn get-rdf-article-content [xml-tree]
  (get-element-val (fn [x] (= :item (:tag x))) (get-rdf-feed-content xml-tree)))

(defn get-atom-article-content [xml-tree]
  (get-element-val (fn [x] (= :entry (:tag x))) (get-atom-feed-content xml-tree)))


;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Multimethods extract different feed formats to a common state.
;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
(defmulti get-feed-content
  (fn [type _]
    (cond
      (or (= type :RSS) (= type :rss)) :rss
      (or (= type :rdf) (= type :RDF)) :rss
      (or (= type :feed) (= type :FEED)) :atom
      :else :unknown)))

(defmethod get-feed-content :rss [_ feed-data]
  (get-rss-feed-content feed-data))

(defmethod get-feed-content :atom [_ feed-data]
  (get-atom-feed-content feed-data))

(defmethod get-feed-content :default [_ feed-data]
  feed-data)


(defmulti get-article-content
  (fn [type _]
    (cond
      (or (= type :RSS) (= type :rss)) :rss
      (or (= type :rdf) (= type :RDF)) :rdf
      (or (= type :feed) (= type :FEED)) :atom
      :else :unknown)))

(defmethod get-article-content :rss [_ feed-data]
  (get-rss-article-content feed-data))

(defmethod get-article-content :rdf [_ feed-data]
  (get-rdf-article-content feed-data))

(defmethod get-article-content :atom [_ feed-data]
  (get-atom-article-content feed-data))

(defmethod get-article-content :default [_ feed-data]
  feed-data)


;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Functions to extract feed details.
;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(def get-feed-details (get-feed-data-by-tags #{:title :link :updated :description :language :updatePeriod :updateFrequency}))

(defn get-feed-detail
  "Takes a feed type and feed data and returns the feeds details as a map."
  [type feed-data]
  (let [details (get-feed-details (get-feed-content type feed-data))]
    (reduce flatten-details {} details)))


;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
;; Functions to extract article details.
;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(def get-article-details (get-feed-data-by-tags #{:title :link :pubDate :creator :category :description :published :content :author}))

(defn format-article-details [article]
  (reduce flatten-details {} article))

(defn get-articles
  [type feed-data]
  (let [articles (get-article-content type feed-data)]
    (map (fn [article]
     (format-article-details
      (get-article-details
        (:content article)))) articles)))


(defn fetch-feed [str-id req]
    (println "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
    (let [id (bigint str-id)
          feed-data (data-access/get-source-feed-data (get-feed-url id))]
       (let [feed-type (:tag feed-data)
             feed-details (get-feed-detail feed-type feed-data)
             ; feed-details (get-rss-feed-details (get-rss-feed-content feed-data))
             ; article-details (get-rss-feed-articles-details (get-rss-feed-articles feed-data))
             article-details (get-articles feed-type feed-data)]
          ; (println "**" feed-type "**")
          ; (pp/pprint feed-details)
          (pp/pprint article-details)
          {:id id
           :feed feed-details
           :articles article-details})))
