;;FIXME: Better name for namespace
(ns preview.handler.example
  (:require [preview.config :refer :all]
            [preview.repository :refer [commits]]
            [compojure.core :refer :all]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [me.raynes.fs :as fs]
            [net.cgrand.enlive-html :as html])
  (:import [java.io File]))


(defn- make-commit-image-url [repo-name commit]
  (str "/screenshot/" repo-name "/" commit ".png"))

(defn- make-commit-image [repo-name commit]
  {:tag :a
   :content [{:tag :img
              :attrs {:src (make-commit-image-url repo-name commit)
                      :width 300}}]})

(defn- commit-screenshots [repo-name]
  (defn- make-repo-screenshots [[branch commits]]
    {:tag :div
     :content
     [{:tag :h2 :content branch}
      {:tag :div
       :content
       (map #(make-commit-image repo-name %) commits)}]})
  (let [repo-commits (commits repo-name)]
    (map make-repo-screenshots repo-commits)))

(defn- make-repo-link [repo-path]
  (let [name (fs/base-name repo-path)
        url (str "/repository/" name "/index.html")]
    {:tag :a :attrs {:href url} :content name}))

(defn- make-repo-div [repo-path]
  {:tag :div
   :attrs {:class "repo"}
   :content [(make-repo-link repo-path)]})

(defn- make-branch-option [branch]
  {:tag :option
   :attrs {:value branch}
   :content branch})

(defn- inject-preview-js [f]
  (html/deftemplate index-page f [s]
    [:body] (html/append s))
  (html/defsnippet banner-template "../resources/preview/handler/example/banner.html"
    [:#preview-banner]
    [])
  (index-page (banner-template)))

(html/deftemplate main-template "../resources/preview/handler/example/example.html"
  [repos]
  [:head :title] (html/content "Watched Repositories")
  [:body :h1] (html/content "Watched Repositories")
  [:#repositories] (html/content (map make-repo-div repos)))

(html/deftemplate screenshot-template "../resources/preview/handler/example/screenshots.html"
  [repo-name]
  [:head :title] (html/content (str "Screenshots for " repo-name))
  [:body :h1] (html/content (str "Screenshots for " repo-name))
  [:#screenshots] (html/content (commit-screenshots repo-name)))

(defmethod ig/init-key :preview.handler/example [_ options]
  (context "/" []
           ;; Listing of repositories
           (GET "/" []
                (let [repos (filter (fn [x] (fs/exists? (fs/file x "index.html")))
                                    (fs/list-dir repository-root))]
                  (main-template repos)))

           ;; Repository view
           (GET "/repository/*" {{file-path :*} :route-params}
                (when (fs/exists? (File. repository-root file-path))
                  (let [f (io/file repository-root file-path)]
                    (if (= (fs/extension file-path) ".html")
                      (inject-preview-js f)
                      f))))

           ;; Screenshot listing view
           (GET "/screenshots/:repo-name" [repo-name]
                (screenshot-template repo-name))

           ;; Screenshot files
           (GET "/screenshot/:repo-name/:image" [repo-name image]
                ;; FIXME: screenshot-root should be separate
                (io/file repository-root "screenshots" repo-name image))))
