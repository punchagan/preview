;; Code to manage the repositories we are managing
(ns preview.repository
  (:require [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as gq]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [me.raynes.fs :as fs]
            [preview.config :refer [repository-root]]
            [tentacles.repos :as tr])
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

(defn- clone [repo-name dest]
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


;; GitHub API

(defn- has-index?
  "Return true if the repo contains an index.html"
  [user repo-name]
  (contains? (tr/contents user repo-name "index.html" {:str? true}) :content))

(defn- repos-with-index
  "Return list of repos which have an index.html"
  [username]
  ;; FIXME: pagination
  (let [repos (tr/user-repos username)]
    (filter #(has-index? username (:name %)) repos)))

(defn- cloned? [repo]
  (fs/exists? (str (io/file repository-root (:name repo)))))

(defn- update-repo [metadata]
  (let [repo-name (:name metadata)]
    (println (str "Updating " repo-name))
    (with-repo repo-name
      (git/git-fetch-all repo))))

(defn- clone-repo [metadata]
  (let [repo-name (:name metadata)
        url (:git_url metadata)
        local-dir (str (io/file repository-root repo-name))]
    (println (str "Cloning " repo-name))
    (git/git-clone url local-dir)))

(defn- clone-or-update [repo]
  (if (cloned? repo)
    (update-repo repo)
    (clone-repo repo)))

(defn clone-and-update-repos [username]
  (doall (map clone-or-update (repos-with-index username))))
