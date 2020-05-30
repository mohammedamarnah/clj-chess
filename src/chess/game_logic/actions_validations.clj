(ns chess.game-logic.actions-validations
  (:require [chess.game-logic.helpers :as ch]))

(defn move-validations [gs player-index {step :step :as args}]
  (let [pawn (get-in (:board gs) (first step))
        error (cond
                (not= player-index (:player_turn gs))
                :not-your-turn
                (not= player-index (first pawn))
                :not-your-pawn
                (not (some #(and (= (first %) (first step))
                                 (contains? (set (second %))
                                            (second step))) (:legal_moves gs)))
                :invalid-move
                (:promotion gs)
                :invalid-move-promotion)]
    (if error
      (throw (ex-info "GameError" {:cause error})))))

(defn set-validations [gs player-index {pos :pos pawn :pawn :as args}]
  (let [cur-pawn (get-in (:board gs) pos)
        error (cond
                (not= player-index (:player_turn gs))
                :not-your-turn
                (not= player-index (first cur-pawn))
                :not-your-pawn
                (not= "p" (second cur-pawn))
                :invalid-pawn
                (not (contains? (set ["q" "r" "n" "b"]) pawn))
                :invalid-promotion
                (not (or (= 7 (first pos))
                         (zero? (first pos))))
                :invalid-pos)]
    (if error
      (throw (ex-info "GameError" {:cause error})))))
