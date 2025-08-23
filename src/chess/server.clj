(ns chess.server
  (:require
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [chess.game-controller :refer [handle-game-msg]]
   [chess.chat-controller :refer [handle-chat-msg]]
   [compojure.core :refer [GET defroutes]]
   [chess.auth :refer [authorize parse-token]]
   [org.httpkit.server :refer [send! with-channel on-close on-receive]]))

(defonce users-conns (atom {}))
(defonce conns (atom {}))

(defn resp [type msg conn]
  (send! conn (json/encode {:type type
                            :payload msg})))

(defn disconnect! [conn _]
  (log/info "user disconnected: " (conns conn))
  (swap! users-conns dissoc (conns conn))
  (swap! conns dissoc conn))

(defn connect! [request conn]
  (if-let [user-id (authorize (parse-token request))]
    (let [update-conns (fn [conns conn] (if conns (conj conns conn) [conn]))]
      (swap! users-conns update-in [user-id] update-conns conn)
      (swap! conns assoc conn user-id)
      (resp :status "connection_successful" conn))
    (let [_ (resp :status "unauthorized_or_invalid_token" conn)]
      (disconnect! conn nil))))

(defn ping [conn _]
  (resp :status "pong" conn))

(defn game-controller [conn msg]
  (let [uid (@conns conn)]
    (log/info (str "user creating a game: " uid))
    (resp :game (handle-game-msg uid msg) conn)))

(defn chat-controller [conn msg]
  (let [uid (@conns conn)]
    (log/info (str "user sending a chat message: " uid))
    (resp :chat (handle-chat-msg uid msg) conn)))

(defn handle-msg [conn msg]
  (if (@conns conn)
    (let [parsed (json/decode msg)
          call (case (get parsed "type")
                 "ping" ping
                 "game" game-controller
                  "chat" chat-controller
                 (fn [conn _] (resp :error "invalid_service_type" conn)))
          payload (get parsed "payload")]
      (call conn payload))
    (resp :error "unauthorized" conn)))

(defn ws-handler [request]
  (with-channel request conn
    (connect! request conn)
    (on-close conn (partial disconnect! conn))
    (on-receive conn (partial handle-msg conn))))

(defroutes app
  (GET "/" request (ws-handler request)))
