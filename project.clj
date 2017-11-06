(defproject preview "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0-beta4"]
                 [clj-jgit "0.8.10"]
                 [duct/core "0.6.1"]
                 [duct/module.logging "0.3.1"]
                 [duct/module.web "0.6.3"]
                 [enlive "1.1.6"]
                 [me.raynes/fs "1.4.6"]]
  :plugins [[duct/lein-duct "0.10.4"]]
  :main ^:skip-aot preview.main
  :resource-paths ["resources" "target/resources"]
  :prep-tasks     ["javac" "compile" ["run" ":duct/compiler"]]
  :profiles
  {:dev  [:project/dev :profiles/dev]
   :repl {:prep-tasks   ^:replace ["javac" "compile"]
          :repl-options {:init-ns user}}
   :uberjar {:aot :all}
   :profiles/dev {}
   :project/dev  {:source-paths   ["dev/src"]
                  :resource-paths ["dev/resources"]
                  :dependencies   [[integrant/repl "0.2.0"]
                                   [eftest "0.4.0"]
                                   [kerodon "0.9.0"]]}})
