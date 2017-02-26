(ns org.dpctl.commands.core-commands
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [org.dpctl.logger :as logger]
            [org.dpctl.cmd-desc :as desc]
            [clojure.string :as str])
  (:import (java.util.jar Manifest)))

(defn ^{:cmd-desc-fn desc/metadata-desc
        :cmd-desc-params {}
        :category "Core"} version
  "Prints version information"
  [& {:keys [^{:doc "Print build date" :type "boolean" :short-option "b"} build-date]
      :or {:build-date false}}]
  (let [res (.findResource (ClassLoader/getSystemClassLoader) "META-INF/MANIFEST.MF")]
    (if (some? res)
      (let [manifest (new Manifest (io/input-stream res))
            attributes (.getAttributes manifest "dpctl")]
        (logger/debug "Manifest content [META-INF/MANIFEST.MF]:\n%s" (str/replace (slurp res) #"(?m)^" "  "))
        (logger/info "dpctl version: %s" (.getValue attributes "App-Version"))
        (when build-date
          (logger/info "dpctl build date: %s" (.getValue attributes "App-Build-Date"))))
      (logger/info "Manifest file not present"))))

(defn ^{:cmd-desc-fn desc/metadata-desc
        :cmd-desc-params {}
        :category "Core"} usage
  "Prints usage"
  [& {}]
  (logger/info "Usage: dpctl [options] command [command-options]"))
