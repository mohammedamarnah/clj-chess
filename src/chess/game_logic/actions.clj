(ns chess.game-logic.actions
  (:require [chess.game-logic.actions-helpers :as cah]
            [chess.game-logic.actions-validations :as cav]
            [chess.game-logic.helpers :as ch]))

(defn move-pawn [gs player-index {step :step :as args}]
  (cav/move-validations gs player-index args)
  (let [from (first step)
        to (second step)
        board (:board gs)
        pawns-count-before (count (ch/get-pieces board (ch/inc-side player-index)))
        new-board (ch/move-pawn board from to)
        pawns-count-after (count (ch/get-pieces new-board (ch/inc-side player-index)))
        moves-count (if (= pawns-count-before pawns-count-after)
                      (dec (:moves_count gs))
                      50)
        is-promotion (cah/promotion-check board from to)
        player-turn (if is-promotion
                      (:player_turn gs)
                      (mod (inc (:player_turn gs)) 2))]
    (as-> gs gs
      (cah/update-moved gs from (:player_turn gs))
      (cah/update-moved gs to (mod (inc (:player_turn gs)) 2))
      (assoc gs :board new-board)
      (assoc gs :promotion is-promotion)
      (assoc gs :player_turn player-turn)
      (assoc gs :legal_moves (cah/legal-moves (:board gs) (:player_turn gs)))
      (cah/castling-move gs)
      (assoc gs :last_move step)
      (assoc gs :moves_count moves-count)
      (if (cah/game-ended? gs) (assoc gs :state :finished) gs))))

(defn set-pawn [gs player-index {pos :pos pawn :pawn :as args}]
  (cav/set-validations gs player-index args)
  (let [board (:board gs)
        cur-pawn (get-in board pos)
        final-pawn [(first cur-pawn) pawn (last cur-pawn)]
        new-board (ch/set-pawn board pos final-pawn)]
    (-> gs
        (assoc :board new-board)
        (assoc :promotion false)
        (assoc :legal_moves (cah/legal-moves new-board (mod (inc (:player_turn gs)) 2)))
        (assoc :player_turn (mod (inc (:player_turn gs)) 2)))))

(defn new-game [gs uid]
  (as-> gs gs
    (assoc gs :board ch/fill-board)
    (assoc gs :legal_moves (cah/legal-moves gs))
    (assoc-in gs [:players_info 0 :user_id] uid)))

(defn final-result-action [gs]
  (let [board (:board gs)
        p1-pawns (ch/get-pieces board 0)
        p2-pawns (ch/get-pieces board 1)
        scores [(- 16 (count p2-pawns)) (- 16 (count p1-pawns))]
        per-scores (map vector (range (:num_players gs)) scores)]
    (-> gs
        (cah/set-final-result (reverse (sort-by second per-scores))))))
