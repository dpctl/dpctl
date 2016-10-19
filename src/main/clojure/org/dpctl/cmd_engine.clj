(ns org.dpctl.cmd-engine
  (:require [clojure.tools.cli :as cli]
            [org.dpctl.commands.core-commands :as core-commands]
            [org.dpctl.commands.soma-commands :as soma-commands]
            [org.dpctl.commands.sync-commands :as sync-commands]
            [org.dpctl.logger :as logger]
            [org.dpctl.config :as config]))

(def command-packages ["org.dpctl.commands.core-commands"
                       "org.dpctl.commands.soma-commands"
                       "org.dpctl.commands.sync-commands"])

(defn lookup-command
  [namespaces command]
  (logger/debug "Command packages: %s" command-packages)

  (first (filter some?
                 (map #(resolve (symbol (format "%s/%s" % command)))
                      namespaces))))

(defn command-description
  [f args]
  (let [m (meta f)
        desc-fn (:cmd-desc-fn m)
        desc-params (:cmd-desc-params m)]
    (logger/debug "Command function metadata: %s" m)
    (assert (some? desc-fn) "Command description function not specified")

    (let [desc (desc-fn f desc-params args)]
      (logger/debug "Command description: %s" desc)

      desc)))

(defn command-options-summary
  [command args]
  (let [f (lookup-command command-packages command)]
    (when (nil? f)
      (throw (ex-info (format "'%s' is not a dpctl command. See 'dpctl --help'." command) {:command command})))
    (let [desc (command-description f args)
          {:keys [options arguments errors summary]} (cli/parse-opts [] (:cli-options desc) :in-order true)]
      summary)))

(defn execute
  [command args]

  (logger/debug "Execute: %s" command)

  (let [f (lookup-command command-packages command)]
    (when (nil? f)
      (throw (ex-info (format "'%s' is not a dpctl command. See 'dpctl --help'." command) {:command command})))
    (let [desc (command-description f args)
          {:keys [options arguments errors summary]} (cli/parse-opts args (:cli-options desc) :in-order true)]

      (logger/debug "Parsed options: %s" (str options))

      (cond
        errors (throw (ex-info nil {:error errors}))
        :else (apply f (interleave (keys options) (vals options)))))))
