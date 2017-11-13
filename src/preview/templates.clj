(ns preview.templates
  (:require [preview.repository :refer [commits repo-state]]
            [me.raynes.fs :as fs]
            [net.cgrand.enlive-html :as html]))

(defn- make-commit-image-url [repo-name commit]
  (str "/screenshot/" repo-name "/" commit ".png"))

(defn- make-commit-image [repo-name commit & {:keys [width height] :or {width 300 }}]
  {:tag :a
   :content [{:tag :img
              :attrs {:src (make-commit-image-url repo-name commit)
                      :width (or width 300)
                      :height height}}]})

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

(defn- make-repo-link [repo-name]
  (let [url (str "/repository/" repo-name "/index.html")]
    {:tag :a :attrs {:href url :class "column"} :content repo-name}))

(defn- make-repo-row [repo-path]
  (let [name (fs/base-name repo-path)
        repo-state (repo-state name)
        preview-image (make-commit-image name (:current-sha repo-state)
                                         :width 200 :height 200)
        url (str "/repository/" name "/index.html")]
    {:tag :div :attrs {:class "row"}
     :content
     [{:tag :span :attrs {:class "column"} :content [preview-image]}
      (make-repo-link name)]}))

(defn- make-repo-div [repo-path]
  {:tag :div
   :attrs {:class "repo"}
   :content [(make-repo-row repo-path)]})

(defn- make-branch-option [branch]
  {:tag :option
   :attrs {:value branch}
   :content branch})

(defn inject-preview-js [f repo-name]
  (html/deftemplate index-page f [s]
    [:body] (html/append s))
  (index-page [{:tag :iframe
                :attrs {:src (str "/banner?repo=" repo-name)
                        :id "preview-banner"
                        :width "100%"
                        :height "100px"}}
               {:tag :link
                :attrs {:href "/css/banner.css"
                        :rel "stylesheet"
                        :type "text/css"}}]))

(html/deftemplate main-template "../resources/preview/handler/example/example.html"
  [repos]
  [:head :title] (html/content "Preview - Watched Repositories")
  [:body :h1] (html/content "Preview")
  [:#description] (html/content {:tag :p :content "Description of preview"})
  [:#repositories] (html/content (map make-repo-div repos)))

(html/deftemplate banner-template "../resources/preview/handler/example/banner.html"
  [])

(html/deftemplate screenshot-template "../resources/preview/handler/example/screenshots.html"
  [repo-name]
  [:head :title] (html/content (str "Screenshots for " repo-name))
  [:body :h1] (html/content (str "Screenshots for " repo-name))
  [:#screenshots] (html/content (commit-screenshots repo-name)))
