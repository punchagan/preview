;; Code for managing screenshots of repos
(ns preview.screenshot
  (:require [preview.config :refer [repository-root]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [etaoin.api :as e]
            [me.raynes.fs :as fs]))


(defmacro with-image-dir [repo-name & body]
  ;; FIXME: Use a different directory than repository-root
  `(let [~'image-dir (str (io/file repository-root "screenshots" ~repo-name))]
     ~@body))

(defn- capture-screenshot [repo-name commit path]
  (let [url (str "http://localhost:3000/repository/" repo-name "/index.html")]
    (e/with-headless {} driver
      (e/go driver url)
      (e/wait-visible driver {:id :preview-banner})
      (e/js-execute driver "document.querySelector('#preview-banner').remove()")
      (let [body-height "return Math.ceil(document.body.getBoundingClientRect().height)"
            height (e/js-execute driver body-height)]
        (e/set-window-size driver 1920 height))
      (e/screenshot driver path))))

(defn screenshot [repo-name commit]
  (with-image-dir repo-name
    (fs/mkdirs image-dir)
    (let [path (io/file image-dir (str commit ".png"))]
      (when-not (fs/exists? path)
        (capture-screenshot repo-name commit path)))))
