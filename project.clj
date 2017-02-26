(defproject org.dpctl/dpctl "0.2.1-SNAPSHOT"
  :description "DataPower Control Tool"
  :url "http://dpctl.org"
  :scm {:name "git"
        :url "https://github.com/dpctl/dpctl"}
  :license {"name" "Eclipse Public License"
            "url" "http://www.eclipse.org/legal/epl-v10.html"}
  :jar-name "dpctl-%s.jar"
  :uberjar-name "dpctl-standalone-%s.jar"
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"]
  :resource-paths ["src/main/resources"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.trace "0.7.9"]]
  :main org.dpctl.core
  :aot [org.dpctl.core org.dpctl.protocols.classpath.handler]
  :auto-clean false
  :repositories [["releases" {:url "https://clojars.org/repo"
                              :sign-releases false
                              :username :env/CLOJARS_USERNAME
                              :password :env/CLOJARS_PASSWORD}]
                 ["snapshots" {:url "https://clojars.org/repo"
                               :sign-releases false
                               :username :env/CLOJARS_USERNAME
                               :password :env/CLOJARS_PASSWORD}]]
  :manifest {:dpctl {"App-Version" ~(-> "project.clj" slurp read-string (nth 2))
                     "App-Build-Date" ~(java.util.Date.)}}
  :profiles {:uberjar {:aot :all}
             :test {:resource-paths ["src/test/resources"]}}
  :plugins [[lein-bin "0.3.5"]
            [lein-cljfmt "0.5.6"]
            [lein-tar "3.3.0"]
            [lein-cloverage "1.0.9"]]
  :tar {:uberjar false
        :format :tar-gz}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]
                  ["vcs" "push" "--tags"]]
  :bin {:name "dpctl"
        :bin-path "~/bin"
        :bootclasspath true})
