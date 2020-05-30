(ns chess.game-logic.game-state)

(def initial-gs
  {:overall_scores [0 0]
   :player_turn    0
   :king_moved     (vec (repeat 2 false))
   :castle_moved   (vec (repeat 4 false))
   :moves_count    50
   :last_move      nil
   :players_info   (vec (repeat 2 {}))})
