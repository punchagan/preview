(ns preview.handler.views
  (:require [preview.config :refer :all]
            [preview.templates :refer :all]
            [preview.repository :refer [preview-repositories]]
            [clojure.java.io :as io]
            [compojure.core :refer :all]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [me.raynes.fs :as fs])
  (:import [java.io File]))


(defmethod ig/init-key :preview.handler/views [_ options]
  (context "/" []
           ;; Listing of repositories
           (GET "/" []
                (let [repos (preview-repositories)]
                  (main-template repos)))

           ;; Repository view
           (GET "/repository/*" {{file-path :*} :route-params}
                (when (fs/exists? (File. repository-root file-path))
                  (let [f (io/file repository-root file-path)]
                    (if (= (fs/extension file-path) ".html")
                      (inject-preview-js f (-> file-path fs/split first))
                      f))))

           ;; Banner html
           (GET "/banner" []
                (banner-template))

           ;; Screenshot listing view
           (GET "/screenshots/:repo-name" [repo-name]
                (screenshot-template repo-name))

           ;; Screenshot files
           (GET "/screenshot/:repo-name/:image" [repo-name image]
                ;; FIXME: screenshot-root should be separate
                (io/file repository-root ".screenshots" repo-name image))))
