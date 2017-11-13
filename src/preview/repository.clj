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

(defn- branches [repo]
  (map branch-name (git/git-branch-list repo)))

(defn- munge-commit-info [repo commit]
  (let [info (gq/commit-info repo commit)]
    (-> info (select-keys [:author :time :id]) (assoc :time (str (:time info))))))

(defn repo-state [repo-name]
  (with-repo repo-name
    (let [current-commit (-> repo git/git-log first)]
      {:branches (branches repo)
       :current-commit (munge-commit-info repo current-commit)
       :current-branch (git/git-branch-current repo)})))

(defn checkout [repo-name branch]
  (with-repo repo-name
    (git/git-checkout repo branch)
    (repo-state repo-name)))

(defn commits
  "Return the commits in a repo, per branch"
  [repo-name]
  (with-repo repo-name
    (let [branch-names (branches repo)
          branch-commits (map #(map branch-name (git/git-log repo %)) branch-names)]
      (zipmap branch-names branch-commits))))

;; FIXME: Use a clone of the repo for this?
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
