;; Code to manage the repositories we are managing
(ns preview.repository
  (:require [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as gq]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [me.raynes.fs :as fs]
            [preview.config :refer [repository-root]])
  (:import java.io.FileNotFoundException))

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

(defn clone [repo-name dest]
  (let [path (str (io/file repository-root repo-name))]
    (git/git-clone path dest)))

(defn repo-state [repo-name]
  (try
    (with-repo repo-name
      (let [current-commit (-> repo git/git-log first)]
        {:branches (branches repo)
         :current-commit (munge-commit-info repo current-commit)
         :current-branch (git/git-branch-current repo)}))
    (catch FileNotFoundException e (str "Not a git repository") {})))

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
  (defn- walk-fn [repo sha]
    (let [repo-path (->> repo .toString (re-find #"\[([^\[]*?).git\]") last)]
      (git/git-checkout repo sha)
      (apply callable repo-path sha args)))
  (with-repo repo-name
    (let [commit-shas (map branch-name (gq/rev-list repo))
          dest (-> repository-root (str "/.clones/") fs/temp-name (io/file repo-name) str)
          cloned-repo (clone repo-name dest)
          clone-name (fs/base-name dest)
          v (doall (map walk-fn (repeat cloned-repo) commit-shas))]
      (fs/delete-dir dest)
      v)))
