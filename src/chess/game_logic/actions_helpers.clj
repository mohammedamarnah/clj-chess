(ns chess.game-logic.actions-helpers
  (:require [chess.game-logic.helpers :as ch]))

(defn get-moves-arr
  [pawn dirs]
  (case pawn
    (or "q" "k") (ch/king-queen-moves dirs)
    "n" (ch/knight-moves dirs)
    "b" (ch/bishop-moves dirs)
    "r" (ch/rook-moves dirs)
    nil))

(defn check-searcher
  [board pos side dir check depth]
  (if (nil? pos)
    false
    (let [new-pos (dir pos)
          new-cell (get-in board new-pos)]
      (if (and (not= 0 depth) (ch/is-valid-pos? new-pos))
        (cond
          (nil? new-cell)
          (recur board new-pos side dir (or false check) (dec depth))
          (ch/is-enemy? board side new-pos)
          (= (second new-cell) "k")
          :else false)
        false))))

(defn pawn-check-searcher
  [board pos side [up down left right]]
  (let [diagonals [((comp up right) pos) ((comp up left) pos)]
        can-eat   (map #(and (ch/is-enemy? board side %)
                             (= "k" (ch/get-pawn board %))) diagonals)]
    (not (nil? (some true? can-eat)))))

(defn is-checkmate?
  [board side]
  (let [side  (ch/inc-side side)
        enemy-pieces (ch/get-pieces board side)
        dirs (ch/get-position-functions board side)
        is-check (map (fn [p]
                        (let [pos (second p)
                              pawn (str (second (first p)))
                              all-moves (get-moves-arr pawn dirs)
                              depth (if (contains? (set ["k" "n"]) pawn)
                                      1 -1)]
                          (if (= pawn "p")
                            (pawn-check-searcher board pos side dirs)
                            (map (fn [dir]
                                   (check-searcher board pos side dir false depth)) all-moves))))
                            enemy-pieces)]
    (not (nil? (some true? (flatten is-check))))))

(defn moves-searcher
  [board pos side dir moves depth first-pos]
   (let [new-pos  (dir pos)
         new-cell (get-in board new-pos)]
     (if (and (not= 0 depth) (ch/is-valid-pos? new-pos))
       (cond
         (is-checkmate? (ch/move-pawn board first-pos new-pos) side)
         []
         (nil? new-cell)
         (recur board new-pos side dir (merge moves new-pos) (dec depth) first-pos)
         (ch/is-enemy? board side new-pos)
         (merge moves new-pos)
         :else moves)
       moves)))

(defn pawn-legal-moves
  [board pos side [up down left right]]
  (let [is-free?          (partial ch/is-free? board)
        is-enemy?         (partial ch/is-enemy? board side)
        starting-square   (= (first pos) (if (zero? side) 6 1))
        new-pos           [(up pos) (up (up pos)) ((comp up right) pos) ((comp up left) pos)]
        is-not-check      (mapv #(not (and (ch/is-valid-pos? %)
                                           (is-checkmate? (ch/move-pawn board pos %) side))) new-pos)
        one-step-up       (if (and (first is-not-check) (is-free? (up pos))) (up pos) nil)
        two-steps-up      (if (and (second is-not-check)
                                   (not (nil? one-step-up))
                                   starting-square
                                   (is-free? (up one-step-up)))
                            (up one-step-up) nil)
        diagonal-right    (if (and (is-not-check 2) (is-enemy? ((comp up right) pos)))
                            ((comp up right) pos) nil)
        diagonal-left     (if (and (is-not-check 3) (is-enemy? ((comp up left) pos)))
                            ((comp up left) pos) nil)]

    (filterv identity [one-step-up two-steps-up diagonal-right diagonal-left])))

(defn get-legal-moves
  [board pos side all-pos depth]
  (let [all-moves (map (fn [dir]
                         (moves-searcher board pos side dir [] depth pos)) all-pos)]
    (vec (apply concat all-moves))))

(defn legal-moves
  ([gs]
   (legal-moves (:board gs) (:player_turn gs)))
  ([board side]
   (let [my-pieces (ch/get-pieces board side)
         dirs (ch/get-position-functions board side)]
     (filter #(not (empty? (second %)))
             (map (fn [p]
                    (let [pos (second p)
                          pawn (str (second (first p)))
                          all-moves (get-moves-arr pawn dirs)
                          depth (if (contains? (set ["k" "n"]) pawn)
                                  1 -1)]
                      [pos (if (= pawn "p")
                             (pawn-legal-moves board pos side dirs)
                             (get-legal-moves board pos side all-moves depth))]))
                  my-pieces)))))

(defn update-moved [gs pos side]
  (let [board (:board gs)
        cell (get-in board pos)
        pawn (second cell)
        castle (if (nil? cell)
                 -1
                 (int (Math/ceil (/ (nth cell 2) 8))))]
    (cond
      (= pawn "k")
      (assoc-in gs [:king_moved side] true)
      (and (= pawn "r") (not= castle -1))
      (assoc-in gs [:castle_moved castle] true)
      :else gs)))

(defn free-path? [board cur to dir]
  (let [new-pos (dir cur)
        pawn (get-in board new-pos)]
    (if (ch/is-valid-pos? new-pos)
      (cond
        (= cur to)
        true
        (not (nil? pawn))
        false
        (nil? pawn)
        (recur board new-pos to dir))
      false)))

(defn castling-check [gs]
  (let [board (:board gs)
        legal-moves (:legal_moves gs)
        king-moved (:king_moved gs)
        castle-moved (:castle_moved gs)
        side (:player_turn gs)
        [up down left right] (ch/get-position-functions board side)
        my-pieces (ch/get-pieces board side)
        pawn (first (filter #(= "k" (second (first %))) my-pieces))
        pos (second pawn)
        castling-locations [[(first pos) (+ (second pos) 2)]
                            [(first pos) (- (second pos) 3)]]
        castle-locations [[(first pos) (+ (second pos) 3)]
                          [(first pos) (- (second pos) 4)]]
        castles [(get-in board (first castle-locations))
                 (get-in board (second castle-locations))]
        free-paths [(free-path? board pos (first castling-locations) right)
                    (free-path? board pos (second castling-locations) left)]
        castling-right (if (and (= "r" (second (first castles)))
                                (not (castle-moved (int
                                                     (Math/ceil
                                                       (/ (last (first castles)) 8)))))
                                (not (king-moved side))
                                (first free-paths))
                         (first castling-locations) nil)
        castling-left (if (and (= "r" (second (second castles)))
                               (not (castle-moved (int
                                                    (Math/ceil
                                                      (/ (last (second castles)) 8)))))
                               (not (king-moved side))
                               (second free-paths))
                        (second castling-locations) nil)
        king-legal-moves (second (first (filter #(= pos (first %)) legal-moves)))]
    (conj (filter #(not= pos (first %)) legal-moves)
          [pos (filter identity (conj king-legal-moves castling-right castling-left))])))

(defn castling-move [gs]
  (let [new-legal-moves (castling-check gs)]
    (assoc gs :legal_moves (filter #(not (empty? (second %)))
                                   new-legal-moves))))

(defn promotion-check [board from to]
  (let [pawn (get-in board from)]
    (and (= "p" (second pawn))
         (or (= 0 (first to))
             (= 7 (first to))))))

(defn- get-winners-losers [scores]
  (let [winners [(first scores)]]
    (reduce (fn [m score]
              (if (= (second score) (second (first winners)))
                (update m 0 conj score)
                (update m 1 conj score))) [winners []]
            (subvec scores 1))))

(defn set-final-result [gs scores]
  (let [[winners losers] (get-winners-losers (vec scores))
        board (:board gs)
        side (:player_turn gs)]
    (assoc gs :final_result
           (if (not (is-checkmate? board side))
             {:winners [[0 0] [1 0]] :losers [] :draw true}
             {:winners winners :losers losers :draw false}))))

(defn game-ended? [gs]
  (let [legal-moves (:legal_moves gs)
        board (:board gs)
        side (:player_turn gs)
        my-pawns (ch/get-pieces board side)
        enemy-pawns (ch/get-pieces board (ch/inc-side side))]
    (or (empty? legal-moves)
        (and (= 1 (count my-pawns))
             (= 1 (count enemy-pawns))
             (= "k" (second (first (first my-pawns))))
             (= "k" (second (first (first enemy-pawns)))))
        (zero? (:moves_count gs)))))

