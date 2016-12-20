(ns org.dpctl.logger-test
  (:require [org.dpctl.logger :as logger])
  (:import (clojure.lang ExceptionInfo))
  (:use [clojure test]))

(use-fixtures :each
  (fn [f]
    (let [l (logger/get-log-level)]
      (f)
      (logger/set-log-level l))))

(deftest test-default-log-level
  (is (= :info (logger/get-log-level))))

(deftest test-set-log-level
  (is (= :debug (logger/set-log-level :debug))))

(deftest test-set-log-level-assertion
  (is (= :error (logger/set-log-level :error)))
  (is (= :warn (logger/set-log-level :warn)))
  (is (= :info (logger/set-log-level :info)))
  (is (= :debug (logger/set-log-level :debug)))
  (is (= :trace (logger/set-log-level :trace)))
  (is (thrown?  java.lang.AssertionError (logger/set-log-level :invalid))))

(deftest test-error-log-level
  (logger/set-log-level :error)

  (is (= "value\n" (with-out-str (logger/error "value"))))
  (is (= "" (with-out-str (logger/warn "value"))))
  (is (= "" (with-out-str (logger/info "value"))))
  (is (= "" (with-out-str (logger/debug "value"))))
  (is (= "" (with-out-str (logger/trace "value"))))

  (is (= "message\n" (with-out-str (logger/error-exception (new Exception "message")))))
  (is (= "" (with-out-str (logger/warn-exception (new Exception "message")))))
  (is (= "" (with-out-str (logger/info-exception (new Exception "message")))))
  (is (= "" (with-out-str (logger/debug-exception (new Exception "message")))))
  (is (= "" (with-out-str (logger/trace-exception (new Exception "message")))))

  (is (= "message\nerror\n"
         (with-out-str (logger/error-exception (ex-info "message" {:error ["error"]
                                                                   :warn ["warn"]
                                                                   :info ["info"]
                                                                   :debug ["debug"]
                                                                   :trace ["trace"]}))))))

(deftest test-warn-log-level
  (logger/set-log-level :warn)

  (is (= "value\n" (with-out-str (logger/error "value"))))
  (is (= "value\n" (with-out-str (logger/warn "value"))))
  (is (= "" (with-out-str (logger/info "value"))))
  (is (= "" (with-out-str (logger/debug "value"))))
  (is (= "" (with-out-str (logger/trace "value"))))

  (is (= "message\n" (with-out-str (logger/error-exception (new Exception "message")))))
  (is (= "message\n" (with-out-str (logger/warn-exception (new Exception "message")))))
  (is (= "" (with-out-str (logger/info-exception (new Exception "message")))))
  (is (= "" (with-out-str (logger/debug-exception (new Exception "message")))))
  (is (= "" (with-out-str (logger/trace-exception (new Exception "message")))))

  (is (= "message\nerror\nwarn\n"
         (with-out-str (logger/warn-exception (ex-info "message" {:error ["error"]
                                                                  :warn ["warn"]
                                                                  :info ["info"]
                                                                  :debug ["debug"]
                                                                  :trace ["trace"]}))))))

(deftest test-info-log-level
  (logger/set-log-level :info)

  (is (= "value\n" (with-out-str (logger/error "value"))))
  (is (= "value\n" (with-out-str (logger/warn "value"))))
  (is (= "value\n" (with-out-str (logger/info "value"))))
  (is (= "" (with-out-str (logger/debug "value"))))
  (is (= "" (with-out-str (logger/trace "value"))))

  (is (= "message\n" (with-out-str (logger/error-exception (new Exception "message")))))
  (is (= "message\n" (with-out-str (logger/warn-exception (new Exception "message")))))
  (is (= "message\n" (with-out-str (logger/info-exception (new Exception "message")))))
  (is (= "" (with-out-str (logger/debug-exception (new Exception "message")))))
  (is (= "" (with-out-str (logger/trace-exception (new Exception "message")))))

  (is (= "message\nerror\nwarn\ninfo\n"
         (with-out-str (logger/info-exception (ex-info "message" {:error ["error"]
                                                                  :warn ["warn"]
                                                                  :info ["info"]
                                                                  :debug ["debug"]
                                                                  :trace ["trace"]}))))))

(deftest test-debug-log-level
  (logger/set-log-level :debug)

  (is (= "[error] value\n" (with-out-str (logger/error "value"))))
  (is (= "[warn] value\n" (with-out-str (logger/warn "value"))))
  (is (= "[info] value\n" (with-out-str (logger/info "value"))))
  (is (= "[debug] value\n" (with-out-str (logger/debug "value"))))
  (is (= "" (with-out-str (logger/trace "value"))))

  (is (boolean (re-find #"(?s)^\[error\] java.lang.Exception: message\n at org.dpctl.*"
                        (with-out-str (logger/error-exception (new Exception "message"))))))
  (is (boolean (re-find #"(?s)^\[warn\] java.lang.Exception: message\n at org.dpctl.*"
                        (with-out-str (logger/warn-exception (new Exception "message"))))))
  (is (boolean (re-find #"(?s)^\[info\] java.lang.Exception: message\n at org.dpctl.*"
                        (with-out-str (logger/info-exception (new Exception "message"))))))
  (is (boolean (re-find #"(?s)^\[debug\] java.lang.Exception: message\n at org.dpctl.*"
                        (with-out-str (logger/debug-exception (new Exception "message"))))))
  (is (= "" (with-out-str (logger/trace-exception (new Exception "message")))))

  (is (boolean (re-find #"(?s)^\[debug\] clojure.lang.ExceptionInfo: message\n.*\[error\] error\n\[warn\] warn\n\[info\] info\n\[debug\] debug\n$"
                        (with-out-str (logger/debug-exception (ex-info "message" {:error ["error"]
                                                                                  :warn ["warn"]
                                                                                  :info ["info"]
                                                                                  :debug ["debug"]
                                                                                  :trace ["trace"]})))))))

(deftest test-trace-log-level
  (logger/set-log-level :trace)

  (is (= "[error] value\n" (with-out-str (logger/error "value"))))
  (is (= "[warn] value\n" (with-out-str (logger/warn "value"))))
  (is (= "[info] value\n" (with-out-str (logger/info "value"))))
  (is (= "[debug] value\n" (with-out-str (logger/debug "value"))))
  (is (= "[trace] value\n" (with-out-str (logger/trace "value"))))

  (is (boolean (re-find #"(?s)^\[error\] java.lang.Exception: message\n at org.dpctl.*"
                        (with-out-str (logger/error-exception (new Exception "message"))))))
  (is (boolean (re-find #"(?s)^\[warn\] java.lang.Exception: message\n at org.dpctl.*"
                        (with-out-str (logger/warn-exception (new Exception "message"))))))
  (is (boolean (re-find #"(?s)^\[info\] java.lang.Exception: message\n at org.dpctl.*"
                        (with-out-str (logger/info-exception (new Exception "message"))))))
  (is (boolean (re-find #"(?s)^\[debug\] java.lang.Exception: message\n at org.dpctl.*"
                        (with-out-str (logger/debug-exception (new Exception "message"))))))
  (is (boolean (re-find #"(?s)^\[trace\] java.lang.Exception: message\n at org.dpctl.*"
                        (with-out-str (logger/trace-exception (new Exception "message"))))))

  (is (boolean (re-find #"(?s)^\[trace\] clojure.lang.ExceptionInfo: message\n.*\[error\] error\n\[warn\] warn\n\[info\] info\n\[debug\] debug\n\[trace\] trace\n$"
                        (with-out-str (logger/trace-exception (ex-info "message" {:error ["error"]
                                                                                  :warn ["warn"]
                                                                                  :info ["info"]
                                                                                  :debug ["debug"]
                                                                                  :trace ["trace"]})))))))

(deftest test-message-format
  (logger/set-log-level :info)

  (is (= "value x 1\n" (with-out-str (logger/info "value %s %d" "x" 1))))
  (is (= "value x 1\n" (with-out-str (logger/info "value %s %d" "x" 1 :ignore)))))

(deftest test-message-format-error
  (logger/set-log-level :info)

  (is (thrown-with-msg? java.util.MissingFormatArgumentException #"Format specifier '%s'" (logger/info "value %s"))))
