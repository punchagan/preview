(ns preview.handler.api
  (:require [preview.config :refer :all]
            [compojure.core :refer :all]
            [clojure.java.io :as io]
            [integrant.core :as ig]
            [clj-jgit.porcelain :as git]
            [clojure.string :as str]
            [ring.util.response :refer [response content-type]]
            [clojure.data.json :as json]))

(defn- json-response
  "Turn the supplied data into a JSON-encoded Ring response."
  [data]
  (-> (response (json/write-str data))
      (content-type "application/json")))

(defn- branch-name [branch]
  (-> branch .getName (str/replace "refs/heads/" "")))

(defn- repo-state [repo]
  (let [branches (map branch-name (git/git-branch-list repo))
        current-branch (git/git-branch-current repo)]
    {:branches branches
     :current-branch current-branch}))

(defmethod ig/init-key :preview.handler/api [_ options]
  (context "/api" []
           (GET "/repo-state/:repo-name" [repo-name]
                (git/with-repo (str(io/file repository-root repo-name))
                  (json-response (repo-state repo))))

           ;;FIXME: This should really be a POST - need to fix CSRF
           (GET "/branch/:repo-name/:branch" [repo-name branch]
                (git/with-repo (str(io/file repository-root repo-name))
                  (git/git-checkout repo branch)
                  (json-response (repo-state repo))))
           ))
