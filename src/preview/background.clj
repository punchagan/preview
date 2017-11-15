(ns preview.background
  (:require [integrant.core :as ig]
            [preview.config :refer [preview-gh-user]]
            [preview.repository
             :refer
             [clone-and-update-repos update-preview-repos update-screenshots]]
            [tick.clock :refer [clock-ticking-in-seconds now]]
            [tick.core :refer [minutes]]
            [tick.schedule :refer [schedule start stop]]
            [tick.timeline :refer [periodic-seq timeline]]))

(defmethod ig/init-key :preview/background [_ options]
  (let [timeline-5-min (timeline (periodic-seq (now) (minutes 5)))
        timeline-hourly (timeline (periodic-seq (now) (minutes 60)))
        clone-schedule (schedule (fn [x] (clone-and-update-repos preview-gh-user)) timeline-hourly)
        update-schedule (schedule (fn [x] (update-preview-repos)) timeline-5-min)
        screenshot-schedule (schedule (fn [x] (update-screenshots)) timeline-5-min)]
    (println "Starting background jobs...")
    (def schedules [clone-schedule update-schedule screenshot-schedule])
    (doall (map start schedules (repeat (clock-ticking-in-seconds))))))

(defmethod ig/halt-key! :preview/background [_ options]
  (println "Stopping background jobs...")
  (doall (map stop schedules)))
