(ns org.dpctl.config)

(def config (atom {}))

(defn get-config
  "Get config"
  []
  @config)

(defn set-config
  "Set config"
  [val]
  (reset! config val))
