;;FIXME: Better name for namespace
(ns preview.handler.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [integrant.core :as ig])
  (:import [java.io File]))

(def config
  (ig/read-string (slurp "dev/resources/local.edn")))

(def repository-root (:repository-root (:preview.handler/example config)))

(when (nil? repository-root)
  (throw (Throwable. "Set :repository-root in dev/resources/local.edn (under :preview.handler/example)")))

(defmethod ig/init-key :preview.handler/example [_ options]
  (context "/" []
           ;; Listing of repositories
           (GET "/" []
                (io/resource "preview/handler/example/example.html"))

           ;; Repository view
           (GET "/repository/*" {{file-path :*} :route-params}
                (File. repository-root file-path))))
