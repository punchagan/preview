;; Code to manage the repositories we are managing
(ns preview.repository
  (:require [preview.config :refer [repository-root]]
            [clojure.java.io :as io]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as gq]
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

(defn walk-repo-commits
  "Walk over all commits in the repo, and execute the callable."
  [repo-name callable & args]
  (with-repo repo-name
    (defn- walk-fn [sha]
      (git/git-checkout repo sha)
      (apply callable repo-name sha args))
    (let [commit-shas (map branch-name (gq/rev-list repo))
          current (git/git-branch-current repo)
          v (doall (map walk-fn commit-shas))]
      (git/git-checkout repo current)
      v)))
