(ns preview.handler.example-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [preview.handler.views :as example]
            [ring.mock.request :as mock]))

(deftest smoke-test
  (testing "example page exists"
    (let [handler  (ig/init-key :preview.handler/example {})
          response (handler (mock/request :get "/example"))]
      (is (= 200 (:status response)) "response ok"))))
