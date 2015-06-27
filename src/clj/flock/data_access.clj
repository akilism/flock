(ns flock.data-access
  (:require [com.ashafa.clutch :as clutch]))

(def user-db "http://localhost:5984/flock-users")

(defn get-user-by-id [id])

(defn get-user-by-email [email])

(defn user-exists? [id]
  (clutch/with-db user-db (clutch/document-exists? id)))

(defn save-user [user]
  (clutch/with-db user-db (clutch/put-document user)))

(defn add-feed [[id :id feeds :feeds :as user] feed]
  )

(defn remove-feed [[id :id feeds :feeds :as user] feed-id]
  )

(defn inc-user-id []
  ())
