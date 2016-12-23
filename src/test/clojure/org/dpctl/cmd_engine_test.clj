(ns org.dpctl.cmd-engine-test
  (:require [org.dpctl.cmd-engine :as engine]
            [org.dpctl.logger :as logger]
            [org.dpctl.config :as config])
  (:use [clojure test]))

(use-fixtures :each
  (fn [f]
    (let [l (logger/get-log-level)
          c (config/get-config)]
      (f)
      (config/set-config c)
      (logger/set-log-level l))))

(defn- mock-cmd-1
  [& {}])

(defn- ^{:cmd-desc-fn (fn [f desc-params cmd-options] {:f f :p desc-params :o cmd-options})
         :cmd-desc-params {:param1 "val1"}} mock-cmd-2
  [& {}])

(defn- ^{:cmd-desc-fn (fn [f desc-params cmd-options] {})
         :cmd-desc-params nil} mock-cmd-3
  [& {}]
  (print "mock-cmd-3"))

(defn- ^{:cmd-desc-fn (fn [f desc-params cmd-options] {:cli-options [["-o" "--option <val>" "Option description" :default "default"]]})
         :cmd-desc-params nil} mock-cmd-4
  [& {:as all}]
  (print "mock-cmd-4" all))

(deftest test-lookup-command
  (is (nil? (engine/lookup-command [] "not-defined")))
  (is (nil? (engine/lookup-command [] "mock-cmd-1")))
  (is (nil? (engine/lookup-command ["org.dpctl.cmd.engine-test"] "not-defined")))
  (is (= #'mock-cmd-1 (engine/lookup-command ["org.dpctl.invalid" "org.dpctl.cmd-engine-test"] "mock-cmd-1"))))

(deftest test-command-description
  (is (thrown-with-msg? java.lang.AssertionError
                        #"Command description function not specified"
                        (engine/command-description #'mock-cmd-1 [])))
  (is (= {:f #'mock-cmd-2
          :p {:param1 "val1"}
          :o ["opt1"]}
         (engine/command-description #'mock-cmd-2 ["opt1"])))

  (logger/set-log-level :info)
  (is (= ""
         (with-out-str (engine/command-description #'mock-cmd-2 ["opt1"]))))

  (logger/set-log-level :debug)
  (is (re-matches #"(?s)^\[debug\] Command function metadata: .*\n\[debug\] Command description: .*"
                  (with-out-str (engine/command-description #'mock-cmd-2 ["opt1"])))))

(deftest test-command-options-summary
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"'unknown' is not a dpctl command"
                        (engine/command-options-summary "unknown" [])))
  (with-redefs [engine/command-packages ["org.dpctl.cmd-engine-test"]]
    (is (= ""
           (engine/command-options-summary "mock-cmd-3" [])))
    (is (= "  -o, --option <val>  default  Option description"
           (engine/command-options-summary "mock-cmd-4" [])))))

(deftest test-execute
  (is (thrown-with-msg? clojure.lang.ExceptionInfo
                        #"'unknown' is not a dpctl command"
                        (engine/command-options-summary "unknown" [])))
  (with-redefs [engine/command-packages ["org.dpctl.cmd-engine-test"]]
    (is (re-matches #".*Unknown option:.*--invalid-option.*"
                    (str (ex-data (is (thrown? clojure.lang.ExceptionInfo
                                               (engine/execute "mock-cmd-3" ["--invalid-option" "val"])))))))
    (is (= "mock-cmd-3"
           (with-out-str (engine/execute "mock-cmd-3" []))))
    (is (= "mock-cmd-4 {:option val}"
           (with-out-str (engine/execute "mock-cmd-4" ["--option" "val"]))))))
