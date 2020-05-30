(ns chess.game-logic.helpers)

(defn is-valid-pos? [pos]
  (and (<= 0 (first pos) 7)
       (<= 0 (second pos) 7)))

(defn is-valid-side? [side]
  (or (zero? side) (= 1 side)))

(defn inc-side [side]
  (mod (inc side) 2))

(def init-board (vec (repeat 8 (vec (repeat 8 nil)))))

(def first-row ["r" "n" "b" "q" "k" "b" "n" "r"])

(defn init-sided-row [side from to]
  "Takes a side [b]-black or [w]-white,
  returns the initial pawns for that side"
  {:pre [(is-valid-side? side)]}
  [(mapv #(vec [side %1 %2]) first-row (range from (- to 8)))
   (mapv #(vec [side "p" %]) (range (- to 8) to))])

(def fill-board
  "Returns the initial board, filled"
  (let [black-side (init-sided-row 1 0 16)
        white-side (init-sided-row 0 16 32)]
    (-> init-board
        (assoc 0 (first black-side))
        (assoc 1 (second black-side))
        (assoc 7 (first white-side))
        (assoc 6 (second white-side)))))

(defn inc-row [pos]
  [(inc (first pos)) (second pos)])

(defn dec-row [pos]
  [(dec (first pos)) (second pos)])

(defn inc-col [pos]
  [(first pos) (inc (second pos))])

(defn dec-col [pos]
  [(first pos) (dec (second pos))])

(defn step [step-white step-black]
  "Takes two functions, representing
  a step modification for both black
  and white sides. Returns a function
  that applies the step on the position
  based on the side"
  (fn [board side pos]
    (if (nil? side)
      nil
      (if (zero? side)
        (step-white pos)
        (step-black pos)))))

(def up (step dec-row inc-row))
(def down (step inc-row dec-row))
(def left (step dec-col inc-col))
(def right (step inc-col dec-col))

(defn get-position-functions [board side]
  (map #(partial % board side) [up down left right]))

(defn king-queen-moves
  [[up down left right]]
  [up down left right
   (comp up right) (comp up left) (comp down right) (comp down left)])

(defn bishop-moves
  [[up down left right]]
  [(comp up right) (comp up left) (comp down right) (comp down left)])

(defn knight-moves
  [[up down left right]]
  [(comp up up left) (comp up up right)
   (comp down down left) (comp down down right)
   (comp left left up) (comp left left down)
   (comp right right up) (comp right right down)])

(defn rook-moves
  [[up down left right]]
  [up down left right])

(defn get-pieces [board side]
  "Returns all the pieces of a side on the board"
  {:pre [(is-valid-side? side)]}
  (for [[i row] (map-indexed list board)
        [j cell] (map-indexed list row)
        :when (= (first cell) side)]
    [cell [i j]]))

(defn move-castle [board from to]
  (let [pawn (get-in board from)
        pawn (if (not (nil? pawn))
               (second pawn))
        castle-pos [[(first to) (inc (second to))]
                    [(first to) (dec (second to))]]
        castles [(get-in board (first castle-pos))
                 (get-in board (second castle-pos))]
        board (if (and (not (nil? pawn))
                       (= "k" pawn))
                (if (> (- (second to) (second from)) 1)
                  (-> board
                      (assoc-in (second castle-pos) (first castles))
                      (assoc-in (first castle-pos) nil))
                  (if (< (- (second to) (second from)) -1)
                    (-> board
                        (assoc-in (first castle-pos) (second castles))
                        (assoc-in (second castle-pos) nil))
                    board))
                board)]
    board))

(defn move-pawn [board from to]
  {:pre [(and (is-valid-pos? from)
              (is-valid-pos? to))]}
  (let [board (move-castle board from to)]
    (-> board
        (assoc-in to (get-in board from))
        (assoc-in from nil))))

(defn set-pawn [board pos pawn]
  {:pre [(and (is-valid-pos? pos)
              (is-valid-side? (first pawn)))]}
  (assoc-in board pos pawn))

(defn kill-pawn [board pos]
  {:pre [(is-valid-pos? pos)]}
  (assoc-in board pos nil))

(defn get-pawn [board pos]
  {:pre [(is-valid-pos? pos)]}
  (str (second (get-in board pos))))

(defn is-free? [board pos]
  (and (is-valid-pos? pos)
       (nil? (get-in board pos))))

(defn is-enemy? [board side pos]
  (let [pawn (get-in board pos)]
    (and (not= side (first pawn))
         (not (nil? (first pawn))))))
