(ns preview.config
  (:require
   [integrant.core :as ig]))

(def config
  (ig/read-string (slurp "dev/resources/local.edn")))

(def repository-root (:repository-root (:preview.handler/example config)))

(when (nil? repository-root)
  (throw (Throwable. "Set :repository-root in dev/resources/local.edn (under :preview.handler/example)")))