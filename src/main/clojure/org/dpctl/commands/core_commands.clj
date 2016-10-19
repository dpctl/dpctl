(ns org.dpctl.commands.core-commands
  (:require [org.dpctl.logger :as logger]
            [org.dpctl.cmd-desc :as desc]))

(defn ^{:cmd-desc-fn desc/metadata-desc
        :cmd-desc-params {}
        :category "Core"} version
  "Prints version information"
  [& {:keys [^{:doc "Print build date" :type "boolean" :short-option "d"} date]
      :or {:date false}}]
  (logger/info "dpctl version 0.0.1"))

(defn ^{:cmd-desc-fn desc/metadata-desc
        :cmd-desc-params {}
        :category "Core"} usage
  "Prints usage"
  [& {}]
  (logger/info "Usage: dpctl [options] command [command-options]"))
