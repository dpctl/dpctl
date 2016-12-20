(ns org.dpctl.core
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [clojure.tools.trace :as trace]
            [org.dpctl.logger :as logger]
            [org.dpctl.config :as config]
            [org.dpctl.cmd-engine :as engine])
  (:import (java.util Properties))
  (:gen-class))

(def cli-options
  [["-c" "--config <file>" "Configuration file"
    :default []
    :assoc-fn (fn [m k v] (update-in m [k] conj v))
    :validate [#(.exists (io/as-file %)) "File does not exist"]]
   ["-l" "--log-level <level>" (format "Log level: %s" (str/join ", " (map name logger/log-levels)))
    :default @logger/log-level
    :default-desc (name @logger/log-level)
    :parse-fn keyword
    :validate [#((keyword %) logger/log-levels-set) (format "Accepted log levels: %s" (str/join ", " (map name logger/log-levels)))]]
   ["-h" "--help" "Lists all command line options with a short description"]])

(defn configure-trace
  []
  (if (= (logger/get-log-level) :trace)
    (do (trace/trace-ns 'org.dpctl.core)
        (trace/trace-ns 'org.dpctl.logger)
        (trace/trace-ns 'org.dpctl.config)
        (trace/trace-ns 'org.dpctl.cmd-engine)
        (trace/trace-ns 'org.dpctl.cmd-desc)
        (trace/trace-ns 'org.dpctl.commands.core-commands)
        (trace/trace-ns 'org.dpctl.commands.soma-commands)
        (trace/trace-ns 'org.dpctl.commands.sync-commands)
        (trace/trace-ns 'org.dpctl.java.net)
        (trace/trace-ns 'org.dpctl.java.ssl)
        (trace/trace-ns 'org.dpctl.java.xml)
        (trace/trace-ns 'org.dpctl.util.soma)
        (trace/trace-ns 'org.dpctl.util.src))
    (do (trace/untrace-ns 'org.dpctl.core)
        (trace/untrace-ns 'org.dpctl.logger)
        (trace/untrace-ns 'org.dpctl.config)
        (trace/untrace-ns 'org.dpctl.cmd-engine)
        (trace/untrace-ns 'org.dpctl.cmd-desc)
        (trace/untrace-ns 'org.dpctl.commands.core-commands)
        (trace/untrace-ns 'org.dpctl.commands.soma-commands)
        (trace/untrace-ns 'org.dpctl.commands.sync-commands)
        (trace/untrace-ns 'org.dpctl.java.net)
        (trace/untrace-ns 'org.dpctl.java.ssl)
        (trace/untrace-ns 'org.dpctl.java.xml)
        (trace/untrace-ns 'org.dpctl.util.soma)
        (trace/untrace-ns 'org.dpctl.util.src))))

(defn add-protocol-handler-pkg
  []
  (let [pkgs (System/getProperty "java.protocol.handler.pkgs")]
    (if (nil? pkgs)
      (System/setProperty "java.protocol.handler.pkgs" "org.dpctl.protocols")
      (System/setProperty "java.protocol.handler.pkgs" (str pkgs "|" "org.dpctl.protocols")))))

(defn set-config-properties
  [files]
  (let [properties (new Properties)]
    (with-open [reader (io/reader (io/resource "org/dpctl/reference.properties"))]
      (.load properties reader))
    (reduce #(with-open [reader (io/reader %2)]
               (.load %1 reader)
               %1)
            properties
            files)
    (let [c (reduce #(assoc %1 %2 (.getProperty properties %2))
                    {}
                    (enumeration-seq (.propertyNames properties)))]
      (config/set-config c))))

(defn main-help
  [options-summary]
  (let [usage-file (io/resource "org/dpctl/help/main.txt")]
    (logger/info (slurp usage-file) options-summary "TODO: Implement command-list")))

(defn command-help
  [options-summary command args]
  (let [usage-file (io/resource "org/dpctl/help/command.txt")]
    (logger/info (slurp usage-file) command options-summary (engine/command-options-summary command args))))

(defn -main
  [& args]
  (let [start-time (System/nanoTime)]
    (try
      (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options :in-order true)
            command (first arguments)]
        (logger/set-log-level (get options :log-level :info))
        (configure-trace)

        (logger/debug "Command line arguments: %s" (str args))
        (logger/debug "Parsed options: %s" (str options))
        (logger/debug "Parsed arguments: %s" (str arguments))

        (set-config-properties (:config options))
        (add-protocol-handler-pkg)

        (cond
          errors (throw (ex-info nil {:error errors}))
          (empty? arguments) (main-help summary)
          (:help options) (command-help summary (first arguments) (rest arguments))
          :else (engine/execute (first arguments) (rest arguments))))
      (catch Exception exception (logger/error-exception exception))
      (finally (logger/debug "Execution time: %.3f sec" (/ (double (- (System/nanoTime) start-time)) 1000000000.0))))))
