(defproject preview "0.1.0-SNAPSHOT"
  :description "Preview is a graphical preview tool for git repositories"
  :url "https://github.com/punchagan/preview"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [org.clojure/clojurescript "1.9.946"]
                 [clj-jgit "0.8.10"]
                 [duct/core "0.6.1"]
                 [duct/module.logging "0.3.1"]
                 [duct/module.web "0.6.3"]
                 [enlive "1.1.6"]
                 [etaoin "0.1.8-SNAPSHOT"]
                 [me.raynes/fs "1.4.6"]
                 [irresponsible/tentacles "0.6.1"]
                 [tick "0.3.5"]
                 [duct/module.cljs "0.3.1"]
                 [cljs-http "0.1.44"]
                 [reagent "0.8.0-alpha2"]]
  :plugins [[duct/lein-duct "0.10.4"]]
  :main ^:skip-aot preview.main
  :resource-paths ["resources" "target/resources"]
  :prep-tasks     ["javac" "compile" ["run" ":duct/compiler"]]
  :profiles
  {:dev  [:project/dev :profiles/dev]
   :repl {:prep-tasks   ^:replace ["javac" "compile"]
          :repl-options {:init-ns user
                         :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
   :uberjar {:aot :all}
   :profiles/dev {}
   :project/dev  {:source-paths   ["dev/src"]
                  :resource-paths ["dev/resources"]
                  :dependencies   [[integrant/repl "0.2.0"]
                                   [eftest "0.4.0"]
                                   [kerodon "0.9.0"]]}})
