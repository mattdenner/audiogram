(ns audiogram.core)

(def audible-frequencies
  ^{:private true, :doc "Sequence of sensible audio frequencies for humans"}
  [ 125 250 500 1000 2000 4000 8000 16000])

(defn- mean [a b] (quot (+ a b) 2))

(defn- bisect
  "This is just a method for bisecting the given range using the specified decider function.
  When, given the current value, the `decider-fn` return `true` the bisect moves to the lower
  half of the current range, otherwise if moves to the upper half"
  [initial-first-of-range initial-last-of-range decider-fn]
  (loop [first-of-range initial-first-of-range
         last-of-range  initial-last-of-range]
    (let [mid-of-range  (mean first-of-range last-of-range)]
      (if (or (= first-of-range mid-of-range) (= last-of-range mid-of-range))
        first-of-range
        (if (decider-fn mid-of-range)
          (recur first-of-range mid-of-range)
          (recur mid-of-range last-of-range)))
      )))

(require '[clojure.core.async :as async])
(require '[audiogram.overtone :as sound])

(defn- tone-player
  "This function plays a tone at a given frequency & volume for a given duration.  This
  function actually returns a core.async channel on which the tone signals when it has
  finished playing, sending `false` on the channel."
  [frequency volume duration]
  (sound/tone frequency volume duration)
  (let [timeout (async/timeout (long (* duration 1000)))]
    (async/go
      (async/<! timeout)
      false)))

(defn- end-tone-player
  "During the generation of the audiogram the user might hear the tone before it finishes,
  so we need a way of killing the tone otherwise the next tone will blur into it!"
  []
  (sound/end-tone))

(defn hearing-user
  "This function returns a representation of a user with headphones on, sat in a quiet
  room, and listening for sounds.  It's actually a core.async channel on which `true` is
  sent if the user hears something, and they indicate that by hitting enter on the keyboard."
  []
  (let [ch (async/chan)]
    (async/go
      (while (read-line)
        (async/>! ch true)))
    ch))

(defn- bisect-fn-for-frequency
  "This function returns another function that can be used to test the given user at the
  given frequency at a varying volume.  The returned function returns `true` if the user
  heard the frequency at the volume, `false` otherwise."
  [user frequency]
  (fn [integer-volume]
    (let [tone-done (tone-player frequency (/ integer-volume 100000.0) 5.0)
          result    (async/alts!! [tone-done user])]
      (end-tone-player)
      (first result))))

(defn audiogram-for
  "This function generates an audiogram for the given user.  An audiogram is considered to
  be a sequence of frequency-volume pairs, where the volume is the point at which the user
  can just hear the frequency."
  [user]
  (let [test-human-response-at (partial bisect-fn-for-frequency user)
        bisect-over-range      (partial bisect 0 100000)]
    (map
      (fn [frequency] [frequency (bisect-over-range (test-human-response-at frequency))])
      audible-frequencies)))

(defn -main []
  (audiogram-for (hearing-user)))
