(ns leiningen.dpctl
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn dpctl
  "Generate SOMA commands using the implementation stylesheets"
  [project & args]
  (let [soma-cmds-file (io/file (:root project) "src" "main" "clojure" "org" "dpctl" "commands" "soma_commands.clj")
        resources-path (.toPath (io/file (:root project) "src" "main" "resources"))
        soma-stylesheets (io/file (.toFile resources-path) "org" "dpctl" "stylesheets" "soma")
        stylesheets (file-seq soma-stylesheets)
        cmds (io/file (:root project) "tasks" "leiningen" "soma_commands.txt")
        cmd (io/file (:root project) "tasks" "leiningen" "soma_command.txt")]
    (spit soma-cmds-file (slurp cmds))

    (doseq [stylesheet (sort-by #(.getCanonicalPath %) stylesheets)
            :when (and (.isFile stylesheet)
                       (not (.isHidden stylesheet))
                       (str/ends-with? (.getName stylesheet) ".xsl"))
            :let [name (subs (.getName stylesheet) 0 (- (count (.getName stylesheet)) 4))]]
      (print (format "Generating comand %-60s " (str "'" name "'...")))

      (spit soma-cmds-file "\n" :append true)
      (spit soma-cmds-file (format (slurp cmd)
                                   name
                                   (.relativize resources-path (.toPath stylesheet))) :append true)

      (println "OK"))))
