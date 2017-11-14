(ns preview.client
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.string :as str]))

;; ------------------------
;; Data

(def data (r/atom {:branches []}))

;; ------------------------
;; Helpers

(defn- page-repo-name []
  (-> (.-href js/location)
      (str/split  #"\?")
      last
      (str/split  #"\=")
      last))

(defn- pathname-repo []
  (-> (.-pathname js/location)
      (str/split #"/")
      last))

(defn- switch-branch [e]
  (let [branch (-> e .-target .-value)]
    (go (let [url (str "/api/branch/" (page-repo-name) "/" branch)
              response (<! (http/get url {}))]
          (if (= (:status response) 200)
            (do
              (reset! data (:body response))
              (.reload js/location true))
            ;; FIXME: Better error handling
            (.log js/console response))))))

(defn- async-update-screenshots [e]
  (let [repo-name (pathname-repo)
        url (str "/api/update-screenshots/" repo-name)]
    (go (http/get url {}))))

;; -------------------------
;; Views

(defn branch-drop-down [branches current]
  [:span [:span [:em "Switch branch: "]]
   [:select
    {:on-change switch-branch
     :value current}
    (for [branch branches]
      ^{:key branch}
      [:option {:value branch} branch])]])

(defn commit-navigation [previous next]
  [:span
   [:span [:a {:href previous} "previous"]]
   [:span [:a {:href next} "next"]]])

(defn screenshots-link []
  (let [repo-name (page-repo-name)
        url (str "/screenshots/" repo-name)]
    [:a {:href url
         :class "button button-outline float-right"
         :target "_top"}
     "Screenshots"]))

(defn repo-name [name]
  [:span {:class "repo-name name"} [:u name]])

(defn home-page [data]
  [:div [:span [:strong {:class "name preview-name"}
                [:a {:href "/"
                     :target "_top"} "Preview"]]]
   [repo-name (page-repo-name)]
   [branch-drop-down (:branches @data) (:current-branch @data)]
   [screenshots-link]])

(defn update-screenshots []
  [:a
   {:class "button button-clear column"
    :href "#"
    :on-click async-update-screenshots}
   "Update Screenshots"])


;; -------------------------
;; Initialize app

(defn mount-banner-root []
  (r/render [home-page data] (.getElementById js/document "preview-banner")))

(defn mount-screenshot-root []
  (r/render [update-screenshots] (.getElementById js/document "update-screenshots")))

(defn get-repo-state []
  (go (let [url (str "/api/repo-state/" (page-repo-name))
            response (<! (http/get url {}))]
        (when (= (:status response) 200)
          (reset! data (:body response))))))

(defn init! []
  (if (.getElementById js/document "preview-banner")
    (do
      (mount-banner-root)
      (get-repo-state))
    (mount-screenshot-root)))

;; FIXME: We are not using figwheel, etc., here. There might be a better way of
;; doing this.
(init!)
