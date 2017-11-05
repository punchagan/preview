;;FIXME: Better name for namespace
(ns preview.handler.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [me.raynes.fs :as fs])
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
                (let [repos (filter (fn [x] (fs/exists? (fs/file x "index.html")))
                                    (fs/list-dir repository-root))]
                  ;; FIXME: Show proper links to projects
                  repos))

           ;; Repository view
           (GET "/repository/*" {{file-path :*} :route-params}
                (File. repository-root file-path))))
