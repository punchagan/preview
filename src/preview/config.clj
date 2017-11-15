(ns preview.config
  (:require
   [integrant.core :as ig]))

(def config
  (ig/read-string (slurp "dev/resources/local.edn")))

(def preview-root (:preview-root (:preview.handler/views config)))
(def preview-gh-user (:preview-gh-user (:preview.handler/views config)))

(when (nil? preview-root)
  (throw (Throwable. "Set :preview-root in dev/resources/local.edn (under :preview.handler/views)")))
