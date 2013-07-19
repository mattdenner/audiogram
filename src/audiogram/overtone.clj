(ns audiogram.overtone)

(require '[overtone.live :as overtone :only [definst env-gen lin-env sin-osc FREE kill]])

; Simple sine wave
(overtone/definst sine-wave [freq 440 attack 0.01 sustain 1.0 release 0.1 vol 0.4]
  (* (overtone/env-gen (overtone/lin-env attack sustain release) 1 1 0 1 overtone/FREE)
     (overtone/sin-osc freq)
     vol))

(defn tone
  [frequency volume duration]
  (println "Playing " frequency "Hz at " volume "...")
  (sine-wave {:freq frequency, :vol volume, :sustain duration}))

(defn end-tone []
  (overtone/kill sine-wave))
