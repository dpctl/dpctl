(ns org.dpctl.logger
  (:require [clojure.string :as str]
            [clojure.stacktrace :as stacktrace]))

(def log-levels [:error :warn :info :debug :trace])

(def log-levels-set (set log-levels))

(def log-level (atom :info))

(defn get-log-level
  "Get log level"
  []
  @log-level)

(defn set-log-level
  "Set log level"
  [val]
  (assert (val log-levels-set))
  (reset! log-level val))

(defn log
  "Log message"
  [level message args]
  (assert (keyword? level))
  (when message
    (when (@log-level #{:debug :trace})
      (print (format "[%s] " (name level))))
    (println (apply format message args))))

(defn error
  "Log a message with error log level"
  [message & args]
  (when (@log-level #{:error :warn :info :debug :trace})
    (log :error message args)))

(defn warn
  "Log a message with warn log level"
  [message & args]
  (when (@log-level #{:warn :info :debug :trace})
    (log :warn message args)))

(defn info
  "Log a message with info log level"
  [message & args]
  (when (@log-level #{:info :debug :trace})
    (log :info message args)))

(defn debug
  "Log a message with debug log level"
  [message & args]
  (when (@log-level #{:debug :trace})
    (log :debug message args)))

(defn trace
  "Log a message with tace log level"
  [message & args]
  (when (@log-level #{:trace})
    (log :trace message args)))

(defn log-exception
  "Log exception"
  [level exception]
  (assert (keyword? level))
  (when (.getMessage exception)
    (when (@log-level #{:debug :trace})
      (print (format "[%s] " (name level))))
    (cond
      (= @log-level :error) (println (.getMessage exception))
      (= @log-level :warn) (println (.getMessage exception))
      (= @log-level :info) (println (.getMessage exception))
      (= @log-level :debug) (stacktrace/print-stack-trace exception)
      (= @log-level :trace) (stacktrace/print-cause-trace exception))))

(defn error-exception
  "Log an exception with error log level"
  [exception]
  (when (@log-level #{:error :warn :info :debug :trace})
    (log-exception :error exception)))

(defn warn-exception
  "Log an exception with warn log level"
  [exception]
  (when (@log-level #{:warn :info :debug :trace})
    (log-exception :warn exception)))

(defn info-exception
  "Log an exception with info log level"
  [exception]
  (when (@log-level #{:info :debug :trace})
    (log-exception :info exception)))

(defn debug-exception
  "Log an exception with debug log level"
  [exception]
  (when (@log-level #{:debug :trace})
    (log-exception :debug exception)))

(defn trace-exception
  "Log an exception with tace log level"
  [exception]
  (when (@log-level #{:trace})
    (log-exception :trace exception)))

(defn log-exception-info
  "Log exception info"
  [level exception]
  (log-exception level exception)
  (let [d (ex-data exception)]
    (doseq [i (:error d)]
      (error i))
    (doseq [i (:warn d)]
      (warn i))
    (doseq [i (:info d)]
      (info i))
    (doseq [i (:debug d)]
      (debug i))
    (doseq [i (:trace d)]
      (trace i))))

(defn error-exception-info
  "Log an exception info with error log level"
  [exception]
  (when (@log-level #{:error :warn :info :debug :trace})
    (log-exception-info :error exception)))

(defn warn-exception-info
  "Log an exception info with warn log level"
  [exception]
  (when (@log-level #{:warn :info :debug :trace})
    (log-exception-info :warn exception)))

(defn info-exception-info
  "Log an exception info with info log level"
  [exception]
  (when (@log-level #{:info :debug :trace})
    (log-exception-info :info exception)))

(defn debug-exception-info
  "Log an exception info with debug log level"
  [exception]
  (when (@log-level #{:debug :trace})
    (log-exception-info :debug exception)))

(defn trace-exception-info
  "Log an exception info with tace log level"
  [exception]
  (when (@log-level #{:trace})
    (log-exception-info :trace exception)))
