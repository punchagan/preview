(ns preview.templates
  (:require [preview.repository :refer [commits repo-state]]
            [me.raynes.fs :as fs]
            [net.cgrand.enlive-html :as html]))

(defn- make-commit-image-url [repo-name commit]
  (str "/screenshot/" repo-name "/" commit ".png"))

(defn- make-commit-image [repo-name commit & {:keys [width height] :or {width 300 }}]
  {:tag :a
   :content [{:tag :a
              :attrs {:href (str "/screenshots/" repo-name)}
              :content [{:tag :img
                         :attrs {:src (make-commit-image-url repo-name commit)
                                 :width (or width 300)
                                 :height height}}]}]})

(defn- make-commit-image-with-link [repo-name commit & {:keys [width height] :or {width 300 }}]
  {:tag :a
   :content [{:tag :a
              :attrs {:href (make-commit-image-url repo-name commit)}
              :content [{:tag :img
                         :attrs {:src (make-commit-image-url repo-name commit)
                                 :class "repo-screenshot"
                                 :width (or width 300)
                                 :height height}}]}]})

(defn- commit-screenshots [repo-name]
  (defn- make-repo-screenshots [[branch commits]]
    {:tag :div
     :content
     [{:tag :h2 :content branch}
      {:tag :div
       :content
       (map #(make-commit-image-with-link repo-name %) commits)}]})
  (let [repo-commits (commits repo-name)]
    (map make-repo-screenshots repo-commits)))

(defn- make-repo-link [repo-name]
  (let [url (str "/repository/" repo-name "/index.html")]
    {:tag :a :attrs {:href url} :content repo-name}))

(defn- make-current-commit-info [author time]
  {:tag :span :content [{:tag :span :attrs {:class "row"} :content author}
                        {:tag :span :attrs {:class "row"} :content (str time)}]})

(defn- make-repo-row [repo-info]
  (let [name (:name repo-info)
        sha (-> repo-info :current-commit :id)
        time (-> repo-info :current-commit :time)
        author (-> repo-info :current-commit :author)
        preview-image (make-commit-image
                       name sha :width 200 :height 200)
        url (str "/repository/" name "/index.html")]
    {:tag :tr
     :content [{:tag :td :content [preview-image]}
               {:tag :td :content [(make-repo-link name)]}
               {:tag :td :content [(make-current-commit-info author time)]}]}))

(defn- make-repo-table [repos]
  {:tag :table
   :content [{:tag :thead
              :content [{:tag :tr
                         :content [{:tag :th :content "Last commit screenshot"}
                                   {:tag :th :content "Repo name"}
                                   {:tag :th :content "Last commit info"}]}]}
             {:tag :tbody :content (map make-repo-row repos)}]})

(defn- make-branch-option [branch]
  {:tag :option
   :attrs {:value branch}
   :content branch})

(defn- navigation [repo-name]
  {:tag :div
   :attrs {:class :row}
   :content [{:tag :a
              :attrs {:href "/"
                      :class "button button-clear column"}
              :content "Project list"}
             {:tag :a
              :attrs {:href (str "/repository/" repo-name "/index.html")
                      :class "button button-clear column"}
              :content "View Project"}
             {:tag :span
              :attrs {:id "update-screenshots"}}]})

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
  [:#description] (html/content {:tag :p :content "Preview is a library to visually preview git repositories"})
  [:#repositories] (html/content (make-repo-table repos)))

(html/deftemplate banner-template "../resources/preview/handler/example/banner.html"
  [])

(html/deftemplate screenshot-template "../resources/preview/handler/example/screenshots.html"
  [repo-name]
  [:head :title] (html/content (str "Screenshots for " repo-name))
  [:body :h1] (html/content (str "Screenshots for " repo-name))
  [:#nav-bar] (html/content (navigation repo-name))
  [:#screenshots] (html/content (commit-screenshots repo-name)))
