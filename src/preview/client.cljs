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
      (butlast)
      (last)))

;; -------------------------
;; Views

(defn branch-drop-down [branches]
  [:select
   (for [branch branches]
     ^{:key branch} [:option {:value branch} branch])])

(defn commit-navigation [previous next]
  [:span
   [:span [:a {:href previous} "previous"]]
   [:span [:a {:href next} "next"]]])

(defn home-page [data]
  [:div [:span "Preview"]
   [branch-drop-down (:branches @data)]
   [commit-navigation (:previous @data) (:next @data)]])

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
