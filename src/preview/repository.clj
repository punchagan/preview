;; Code to manage the repositories we are managing
(ns preview.repository
  (:require [preview.config :refer [repository-root]]
            [clojure.java.io :as io]
            [clj-jgit.porcelain :as git]
            [clojure.string :as str]))

(defmacro with-repo [repo-name & body]
  `(git/with-repo (str (io/file repository-root ~repo-name))
     ~@body))

(defn- branch-name [branch]
  (-> branch .getName (str/replace "refs/heads/" "")))

(defn repo-state [repo-name]
  (with-repo repo-name
    (let [branches (map branch-name (git/git-branch-list repo))
          current-branch (git/git-branch-current repo)]
      {:branches branches
       :current-branch current-branch})))

(defn checkout [repo-name branch]
  (with-repo repo-name
    (git/git-checkout repo branch)
    (repo-state repo-name)))
