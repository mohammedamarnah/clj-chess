(ns chess.game-controller
  (:require
   [clojure.walk :as walk]
   [chess.game-logic.game-state :refer [initial-gs]]
   [chess.game-logic.actions :refer [new-game move-pawn]]
   [chess.game-logic.random-ai :as ai]))

(defn join-game [uid])

(defn move-pawn-handler [msg]
  (let [gs (walk/keywordize-keys (get msg "gs"))
        player-index (get msg "player_index")
        args (walk/keywordize-keys (get msg "args"))
        new-gs (move-pawn gs player-index args)]
    (if (empty? (get-in new-gs [:players_info 1]))
      (let [ai-move (ai/move-pawn new-gs)]
        (move-pawn new-gs 1 (last ai-move)))
      new-gs)))

(defn handle-game-msg [uid msg]
  (case (get msg "action")
    "new_game" (new-game initial-gs uid)
    "join_game" (join-game uid)
    "move" (move-pawn-handler msg)
    {:error "unknown_action"}))
