(defproject org.dpctl/dpctl "0.2.0-SNAPSHOT"
  :description "DataPower Control"
  :url "https://github.com/org.dpctl/dpctl"
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
  :profiles {:uberjar {:aot :all}
             :test {:resource-paths ["src/test/resources"]}}
  :plugins [[lein-bin "0.3.5"]]
  :bin {:name "dpctl"
        :bin-path "~/bin"
        :bootclasspath true})
