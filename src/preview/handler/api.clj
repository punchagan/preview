(ns preview.handler.api
  (:require [clojure.core.async :as async]
            [preview.repository :refer :all]
            [preview.screenshot :refer [screenshot]]
            [compojure.core :refer :all]
            [integrant.core :as ig]
            [ring.util.response :refer [response content-type]]
            [clojure.data.json :as json]))

(defn- json-response
  "Turn the supplied data into a JSON-encoded Ring response."
  [data]
  (-> (response (json/write-str data))
      (content-type "application/json")))

(defmethod ig/init-key :preview.handler/api [_ options]
  (context "/api" []
           (GET "/repo-state/:repo-name" [repo-name]
                (json-response (repo-state repo-name)))

           ;;FIXME: This should really be a POST - need to fix CSRF
           (GET "/branch/:repo-name/:branch" [repo-name branch]
                (json-response (checkout repo-name branch)))

           ;; Update screenshots
           (GET "/update-screenshots/:repo-name" [repo-name]
                (async/go (walk-repo-commits repo-name screenshot))
                (json-response (repo-state repo-name)))

           ;; Update repositories
           (GET "/update-repos/:username" [username]
                (async/go (clone-and-update-repos username))
                (json-response {:username username}))))
