(ns flock.server
  (:use [org.httpkit.server :only [run-server]])
  (:require [clojure.java.io :as io]
            [flock.dev :refer [is-dev? inject-devmode-html browser-repl start-figwheel]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [resources]]
            [net.cgrand.enlive-html :refer [deftemplate]]
            [net.cgrand.reload :refer [auto-reload]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :as ring-json]
            [ring.util.response :as rr]
            [flock.feed :as feed]))

(deftemplate page (io/resource "index.html") []
  [:body] (if is-dev? inject-devmode-html identity))

(defroutes routes
  (resources "/")
  (resources "/react" {:root "react"})
  (GET "/api/feeds" req (rr/response (feed/fetch-feeds req)))
  (GET "/api/feeds/:id{[0-9]+}" [id :as req] (rr/response (feed/fetch-feed id req)))
  (GET "/*" req (clojure.string/join "" (page))))

(def http-handler
  (if is-dev?
    (-> #'routes
        (reload/wrap-reload)
        (ring-json/wrap-json-response)
        (wrap-defaults (assoc site-defaults :proxy true)))
    ; (reload/wrap-reload (wrap-defaults #'routes api-defaults))
    (-> routes
      (ring-json/wrap-json-response)
      (wrap-defaults (assoc site-defaults :proxy true)))))

(defn run-web-server [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (print "Starting web server on port" port ".\n")
    (run-server http-handler {:port port :join? false})))

(defn run-auto-reload [& [port]]
  (auto-reload *ns*)
  (start-figwheel))

(defn run [& [port]]
  (when is-dev?
    (run-auto-reload))
  (run-web-server port))

(defn -main [& [port]]
  (run port))
