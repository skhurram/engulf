(ns engulf.remote-client
  (:require
   [engulf.benchmark :as bench]
   [engulf.ning-client :as http]
   [cheshire.core :as json]
   [carbonite.api :as carb])
  (:use lamina.core))

(def registry (carb/default-registry))

(defn send-snapshot
  [conn benchmarker]
  (bench/current-snapshot))


(def http-client (http/create-client))

(def conn (atom nil))

(defn create-conn
  [host port]
  (http/request-websocket http-client {:url (str "ws://" host ":" port "/benchmarker/slave")}))

(def connect [host port]
     (compare-and-set! conn nil (create-conn host port))
     (receive-all conn
                  (fn [m])))