(ns flock.data-access
  (:require [com.ashafa.clutch :as clutch]
            [clj-http.client :as client]
            [clojure.data.xml :as xml]
            [clojure.java.io :as io]))

; ~~~~~~~~~~~~~~~~~~~~~
; Define the databases.
; ~~~~~~~~~~~~~~~~~~~~~
(def users-db "http://localhost:5984/flock-users")
(def feeds-db "http://localhost:5984/flock-feeds")


; ~~~~~~~~~~~~~~~~~~~~~
; Bootstrap some views.
; ~~~~~~~~~~~~~~~~~~~~~

(defn save-views []
  (clutch/with-db feeds-db
    (clutch/save-view "feeds"
      (clutch/view-server-fns :cljs
        {:by-title-name {:map (fn [doc]
                        (js/emit (aget doc "title") doc)
                        (js/emit (aget doc "name") doc))}}))
    (clutch/save-view "feeds"
      (clutch/view-server-fns :cljs
        {:by-feed-url {:map (fn [doc]
                        (js/emit (aget doc "feedUrl") doc))}})))
  (clutch/with-db users-db
    (clutch/save-view "users"
      (clutch/view-server-fns :cljs
        {:by-email {:map (fn [doc]
                        (js/emit (aget doc "email") doc))}}))))


; ~~~~~~~~~~~~~~~~~~~~
; Getting Data Doggies
; ~~~~~~~~~~~~~~~~~~~~

(defn get-user-by-id [user-id]
  (clutch/with-db users-db
    (clutch/get-document user-id)))

(defn get-user-by-email [email]
  (clutch/with-db users-db
    (clutch/get-view "users" "by-email" {:key feed-name})))

(defn get-feeds [feed-names]
  ; (save-views)
  (clutch/with-db feeds-db
    (clutch/all-documents {:include_docs true})))

(defn get-feed-by-id [feed-id]
  (clutch/with-db feeds-db
    (clutch/get-document feed-id)))

(defn get-feed-by-name [feed-name]
  (clutch/with-db feeds-db
    (clutch/get-view "feeds" "by-id" {:key feed-name})))

(defn get-feeds-by-user [user-id]
  (:feeds (get-user-by-id user-id)))

(defn user-exists? [id]
  (clutch/with-db users-db (clutch/document-exists? id)))


; ~~~~~~~~~~~~~~~~~
; Adding Data Dere.
; ~~~~~~~~~~~~~~~~~

(defn create-user [user]
  (let [result (clutch/with-db users-db (clutch/put-document user))]
    (dissoc (merge user result) :passwd)))

(defn update-user [user]
  (clutch/with-db users-db (clutch/put-document user)))

(defn create-feed [feed]
  (let [result (clutch/with-db feeds-db (clutch/put-document feed))]
    (merge feed result)))

(defn add-feed-db-fields
  [feed]
  (dissoc (assoc feed :feedUrl (:xmlUrl feed) :tags []) :xmlUrl))

(defn create-feeds [feeds]
  (map create-feed (map add-feed-db-fields feeds)))


(defn remove-feed [[id :id feeds :feeds :as user] feed-id]
  )

(defn parse-feed-xml
  [xml-data]
  (let [input-xml (java.io.StringReader. xml-data)]
    (xml/parse input-xml)))

(defn get-source-feed-data
  [url]
  (let [response (client/get url)]
        (parse-feed-xml (:body response))))

(defn get-feeds-file
  [file-path]
  (with-open [reader (io/reader file-path)]
    (parse-feed-xml (clojure.string/join "\n" (line-seq reader)))))
