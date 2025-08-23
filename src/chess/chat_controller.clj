(ns chess.chat-controller)

(defn handle-chat-msg [uid msg]
  (case (get msg "action")
    ;; "send-message" (send-message uid args)
    {:error "unknown_action"}))