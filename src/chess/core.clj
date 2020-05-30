(ns chess.core
  (:require
    [chess.server :refer [app]]
    [environ.core :refer [env]]
    [org.httpkit.server :as http-kit]

    [chess.game-logic.game-state :as gs]
    [chess.game-logic.actions :as ac]
    [chess.game-logic.actions-helpers :as ach]
    [chess.game-logic.actions-validations :as acv]
    [chess.game-logic.random-ai :as ai]
    [chess.game-logic.helpers :as h]

    [clojure.tools.logging :as log])
  (:gen-class))

;contains function that can be used to stop http-kit server
(defonce server (atom nil))

(defn parse-port [[port]]
  (Integer/parseInt (or port (env :port) "8080")))

(defn start-server [port]
  (reset! server (http-kit/run-server app {:port port :thread 32})))

(defn stop-server []
  (when @server
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  (let [port (parse-port args)]
    (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))
    (start-server port)
    (log/info "server started on port: " port)))
