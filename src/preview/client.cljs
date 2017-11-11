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
      (str/split  #"/")
      butlast
      last))

(defn- switch-branch [e]
  (let [branch (-> e .-target .-value)]
    (go (let [url (str "/api/branch/" (page-repo-name) "/" branch)
              response (<! (http/get url {}))]
          (prn (str "Switching to " branch))
          (if (= (:status response) 200)
            (do
              (reset! data (:body response))
              (.reload js/location true))
            ;; FIXME: Better error handling
            (prn response))))))

;; -------------------------
;; Views

(defn branch-drop-down [branches current]
  [:select
   {:on-change switch-branch
    :value current}
   (for [branch branches]
     ^{:key branch}
     [:option {:value branch} branch])])

(defn commit-navigation [previous next]
  [:span
   [:span [:a {:href previous} "previous"]]
   [:span [:a {:href next} "next"]]])

(defn screenshots-link []
  (let [repo-name (page-repo-name)
        url (str "/screenshots/" repo-name)]
    [:a {:href url} "Screenshots"]))

(defn home-page [data]
  [:div [:span "Preview"]
   [branch-drop-down (:branches @data) (:current-branch @data)]
   [screenshots-link]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page data] (.getElementById js/document "preview-app")))

(defn get-repo-state []
  (go (let [url (str "/api/repo-state/" (page-repo-name))
            response (<! (http/get url {}))]
        (when (= (:status response) 200)
          (reset! data (:body response))))))

(defn init! []
  (mount-root)
  (get-repo-state))

;; FIXME: We are not using figwheel, etc., here. There might be a better way of
;; doing this.
(init!)
