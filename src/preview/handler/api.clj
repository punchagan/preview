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

(defmethod ig/init-key :preview.handler/api [_ options]
  (context "/api" []
           (GET "/repo-state/:repo-name" [repo-name]
                (git/with-repo (str(io/file repository-root repo-name))
                  (let [branches (map branch-name (git/git-branch-list repo))]
                    (json-response {:branches branches}))))
           ))
