(ns flock.error)

(defn auth [error]
  {:type 00 :message "Invalid login."})
