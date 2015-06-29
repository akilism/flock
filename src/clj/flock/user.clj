(ns flock.user
  (:require [flock.data-access :as data-access]
            [flock.error :as error]))

; User data structure
; (def user { :id (java.util.UUID/randomUUID)
;     :email "some@email.address"
;     :password "some-encrypted-password"
;     :salt "some-salt-value"
;     :last-login "some-date"
;     :auth-token "some-auth-token"
;     :feeds []})

(defn check-valid-last-login [last-login]
  ;;if last login > X days return false
  true)

(defn encrypt-pass [password salt]
  )

(defn pass-auth [password [salt :salt user-pass :password]]
  ;; authenticate the user using a password.
  (= user-pass (encrypt-pass password salt)))

(defn token-auth [auth-token [user-token :auth-token last-login :last-login]]
  ;;authenticate the user using an auth token.
  (cond
    (check-valid-last-login last-login) (if (= user-token auth-token) true false)
    :else false))

(defn is-authentic [auth-value user auth-type]
  (cond
    (nil? user) {:error :invalid}
    (and (= :password auth-type) (pass-auth auth-value user)) user
    (and (= :token auth-type) (token-auth auth-value user)) user
    :else {:error :invalid}))

(defn login-user [email password]
  ;;login the user using an email address and password.
  (let [user (data-access/get-user-by-email email)]
    (is-authentic password user :password)))

(defn get-user [id auth-token]
  ;;get the user using an id and auth token.
  (let [user (data-access/get-user-by-id id)]
    (is-authentic auth-token user :token)))

; (defn add-feed [feed id auth-token]
;   (let [result (get-user id auth-token)]
;     (cond
;       (:error result) (error/auth result)
;       :else (data-access/add-feed result feed))))

