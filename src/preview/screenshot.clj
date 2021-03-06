;; Code for managing screenshots of repos
(ns preview.screenshot
  (:require [preview.config :refer [preview-root]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [etaoin.api :as e]
            [me.raynes.fs :as fs]))


(defmacro with-image-dir [repo-name & body]
  ;; FIXME: Use a different directory than preview-root
  `(let [~'image-dir (str (io/file preview-root ".screenshots" ~repo-name))]
     ~@body))

(defn- capture-screenshot [repo-name commit path]
  (println "Capturing screenshot for " repo-name " " commit)
  (let [url (str "http://localhost:3000/repository/" repo-name "/index.html")]
    (e/with-headless {} driver
      (e/go driver url)
      (try
        (do
          (e/wait-visible driver {:id :preview-banner})
          (e/js-execute driver "document.querySelector('#preview-banner').remove()"))
        (catch Exception e (println "Failred to capture screenshot for " repo-name " " commit)))
      (let [body-height "return Math.ceil(document.body.getBoundingClientRect().height)"
            height (e/js-execute driver body-height)]
        (e/set-window-size driver 1920 height))
      (e/screenshot driver path))))

(defn- url-path [path]
  (str (apply io/file
              (drop (-> preview-root fs/split count)
                    (fs/split path)))))

(defn screenshot [repo-path commit]
  (let [repo-name (fs/base-name repo-path)
        url-path (url-path repo-path)]
    (with-image-dir repo-name
      (fs/mkdirs image-dir)
      (let [path (io/file image-dir (str commit ".png"))]
        (when-not (fs/exists? path)
          (capture-screenshot url-path commit path))))))
