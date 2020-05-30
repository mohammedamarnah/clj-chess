(ns chess.game-logic.random-ai
  (:require [chess.game-logic.helpers :as ch]))

(defn move-pawn [gs & args]
  (let [legal-moves (:legal_moves gs)
        random-pawn (rand-nth legal-moves)
        random-pos (rand-nth (second random-pawn))]
    ["move_pawn" (:player_turn gs) {:step [(first random-pawn) random-pos]}]))

(defn set-pawn [gs & args]
  (let [board (:board gs)
        player-turn (:player_turn gs)
        my-pieces (ch/get-pieces board player-turn)
        my-pieces (filter #(and (= "p" (second (first %)))
                                (or (= 7 (first (second %)))
                                    (= 0 (first (second %))))) my-pieces)
        pos (second (first my-pieces))]
    ["set_pawn" (:player_turn gs) {:pos pos :pawn "q"}]))

(defn in-hand [gs & args]
  (if (:promotion gs)
    (set-pawn gs args)
    (move-pawn gs args)))
