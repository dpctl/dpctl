(ns org.dpctl.core-test
  (:require [org.dpctl.core :as core]
            [org.dpctl.logger :as logger]
            [org.dpctl.config :as config]
            [org.dpctl.cmd-engine :as engine])
  (:use [clojure test]))

(def f1 "src/test/resources/org/dpctl/test1.properties")
(def f2 "src/test/resources/org/dpctl/test2.properties")

(use-fixtures :each
  (fn [f]
    (let [l (logger/get-log-level)
          c (config/get-config)]
      (f)
      (config/set-config c)
      (logger/set-log-level l))))

(deftest test-invalid-option
  (is (re-find #"Unknown option: \"--invalid\"" (with-out-str (core/-main "--invalid" "invalid")))))

(deftest test-invalid-option-value
  (is (re-find #"Failed to validate \"--log-level invalid\"" (with-out-str (core/-main "--log-level" "invalid")))))

(deftest test-log-level-option
  (with-out-str (core/-main))
  (is (= :info (logger/get-log-level)))

  (with-out-str (core/-main "--log-level" "debug"))
  (is (= :debug (logger/get-log-level)))

  (with-out-str (core/-main "--log-level" "invalid"))
  (is (= :info (logger/get-log-level)))

  (with-out-str (core/-main "--invalid-arg" "value" "--log-level" "debug"))
  (is (= :info (logger/get-log-level)))

  (with-out-str (core/-main "--log-level" "debug" "--invalid-arg" "value"))
  (is (= :debug (logger/get-log-level))))

(deftest test-main-help-option
  (with-redefs-fn {#'core/main-help (fn [options-summary] (logger/info "dpctl help"))}
    #(do (is (= "dpctl help\n" (with-out-str (core/-main "--help"))))
         (is (= "dpctl help\n" (with-out-str (core/-main "--help" "--log-level" "info"))))
         (is (= "dpctl help\n" (with-out-str (core/-main)))))))

(deftest test-command-help-option
  (with-redefs-fn {#'core/command-help (fn [options-summary command args] (logger/info "dpctl command help"))}
    #(do (is (= "dpctl command help\n" (with-out-str (core/-main "--help" "command"))))
         (is (= "dpctl command help\n" (with-out-str (core/-main "--help" "--log-level" "info" "command")))))))

(deftest test-config-option
  (with-redefs-fn {#'core/set-config-properties (fn [files]
                                                  (is (= [] files)))}
    #(with-out-str (core/-main)))
  (with-redefs-fn {#'core/set-config-properties (fn [files]
                                                  (is (= [] files)))}
    #(with-out-str (core/-main "command")))
  (with-redefs-fn {#'core/set-config-properties (fn [files]
                                                  (is (= [f1 f2] files)))}
    #(with-out-str (core/-main "--config" f1 "--config" f2 "command"))))

(deftest test-main-help
  (let [s (with-out-str (core/main-help "summary"))]
    (is (re-find #".*Usage: dpctl \[options\] command \[command-options\].*" s))
    (is (re-find #".*Options:\nsummary.*" s))))

(deftest test-command-help
  (with-redefs-fn {#'engine/command-options-summary (fn [command args]
                                                      (is (= "command" command))
                                                      (is (= ["--name" "value"] args))
                                                      "options summary")}
    #(let [s (with-out-str (core/command-help "summary" "command" ["--name" "value"]))]
       (is (re-find #".*Usage: dpctl \[options\] command \[command-options\].*" s))
       (is (re-find #".*Options:\nsummary.*" s))
       (is (re-find #".*Command options:\noptions summary.*" s)))))

(deftest test-set-config-properties
  (core/set-config-properties nil)
  (let [properties (config/get-config)]
    (is (= "src" (properties "src-dir")))
    (is (nil? (properties "undefined"))))

  (core/set-config-properties [])
  (let [properties (config/get-config)]
    (is (= "src" (properties "src-dir")))
    (is (nil? (properties "undefined"))))

  (core/set-config-properties [f1 f2])
  (let [properties (config/get-config)]
    (is (= "override2" (properties "src-dir")))
    (is (= "override1" (properties "temp-dir")))
    (is (nil? (properties "undefined")))))

(deftest test-set-config-properties-error
  (is (thrown-with-msg? java.io.FileNotFoundException #"invalid.properties.*No such file or directory.*"(core/set-config-properties ["invalid.properties"]))))

(deftest test-command
  (with-redefs-fn {#'engine/execute (fn [command args]
                                      (is false "This function should not be called as no command is passed"))}
    #(with-out-str (core/-main)))
  (with-redefs-fn {#'engine/execute (fn [command args]
                                      (is (= "command" command))
                                      (is (= [] args)))}
    #(with-out-str (core/-main "command")))
  (with-redefs-fn {#'engine/execute (fn [command args]
                                      (is (= "command" command))
                                      (is (= ["--name" "val"] args)))}
    #(with-out-str (core/-main "command" "--name" "val"))))