;;FIXME: Better name for namespace
(ns preview.handler.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [me.raynes.fs :as fs]
            [net.cgrand.enlive-html :as html])
  (:import [java.io File]))

(def config
  (ig/read-string (slurp "dev/resources/local.edn")))

(def repository-root (:repository-root (:preview.handler/example config)))

(when (nil? repository-root)
  (throw (Throwable. "Set :repository-root in dev/resources/local.edn (under :preview.handler/example)")))


(defn- make-repo-link [repo-path]
  (let [name (fs/base-name repo-path)
        url (str "/repository/" name "/index.html")]
    {:tag :a :attrs {:href url} :content name}))

(defn- make-repo-div [repo-path]
  {:tag :div
   :attrs {:class "repo"}
   :content [(make-repo-link repo-path)]})

(html/deftemplate main-template "../resources/preview/handler/example/example.html"
  [repos]
  [:head :title] (html/content "Watched Repositories")
  [:body :h1] (html/content "Watched Repositories")
  [:#repositories] (html/content (map make-repo-div repos)))

(defmethod ig/init-key :preview.handler/example [_ options]
  (context "/" []
           ;; Listing of repositories
           (GET "/" []
                (let [repos (filter (fn [x] (fs/exists? (fs/file x "index.html")))
                                    (fs/list-dir repository-root))
                      ]
                  (main-template repos)))

           ;; Repository view
           (GET "/repository/*" {{file-path :*} :route-params}
                (when (fs/exists? (File. repository-root file-path))
                  (File. repository-root file-path)))))
