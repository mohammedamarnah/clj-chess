(ns chess.auth
  (:require [clojure.string :as str]
            [buddy.sign.jws :as jws]
            [cheshire.core :as json]))

(defn parse-token [request]
  (let [query-string (str/split (:query-string request) #"=")
        token (first (keep-indexed (fn [i el]
                                     (if (= el "token")
                                       (get query-string (inc i)))) query-string))]
    (try
      (first (str/split token #"/"))
      (catch Exception e
        nil))))

(defn authorize [token]
  (try
    (let [unsigned-bytes (jws/unsign token (System/getenv "RAKE_SECRET") {:alg :hs256})
          body (apply str (map char unsigned-bytes))
          decoded (json/decode body)]
      (if (contains? decoded "user_id")
        (get decoded "user_id")
        nil))
    (catch Exception e
      nil)))
