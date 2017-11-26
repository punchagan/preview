(ns preview.handler.views
  (:require [preview.config :refer :all]
            [preview.templates :refer :all]
            [preview.repository :refer [preview-repositories checkout]]
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
           (GET "/repository/*" {{file-path :*} :route-params {commit "commit"} :query-params}
                (when (fs/exists? (File. preview-root file-path))
                  (let [f (io/file preview-root file-path)
                        repo-name (-> file-path fs/split first)]
                    (if (= (fs/extension file-path) ".html")
                      (do
                        (when commit
                          (checkout repo-name commit))
                        (inject-preview-js f repo-name))
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
                (io/file preview-root ".screenshots" repo-name image))))
