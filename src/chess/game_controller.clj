(ns chess.game-controller
  (:require
   [clojure.tools.logging :as log]
   [clojure.walk :as walk]
   [chess.game-logic.game-state :refer [initial-gs]]
   [chess.game-logic.actions :refer [new-game move-pawn]]))

(defn join-game [uid])

(defn move-pawn-handler [msg]
  (let [gs (walk/keywordize-keys (get msg "gs"))
        player-index (get msg "player_index")
        args (walk/keywordize-keys (get msg "args"))]
    (log/info msg)
    (log/info player-index)
    (move-pawn gs player-index args)))

(defn handle-game-msg [uid msg]
  (log/info msg)
  (case (get msg "action")
    "new_game" (new-game initial-gs uid)
    "join_game" (join-game uid)
    "move" (move-pawn-handler msg)
    ;; "set-pawn" (set-pawn gs player-index args)
    ;; "final-result" (final-result-action gs)
    {:error "unknown_action"}))