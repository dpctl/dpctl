(ns org.dpctl.commands.soma-commands
  (:require [org.dpctl.cmd-desc :as desc]
            [org.dpctl.util.soma :as soma]
            [org.dpctl.util.io :as io-util]))

(defmacro defsomacmd
  [name stylesheet]
  `(defn ~(vary-meta name assoc :cmd-desc-fn desc/stylesheet-desc :cmd-desc-params {:stylesheet stylesheet})
     [& {:keys [~(vary-meta `dp-mgmt-url assoc :doc "DataPower management url" :short-option "u" :required true)
                ~(vary-meta `dp-user-name assoc :doc "DataPower user name" :short-option "n" :required true)
                ~(vary-meta `dp-user-password assoc :doc "DataPower user password" :short-option "p" :required true)
                ~(vary-meta `domain assoc :doc "DataPower domain" :short-option "d")
                ~(vary-meta `ssl-trusted-certificates assoc :doc "Trusted SSL certificates (fingerprint regex)")
                ~(vary-meta `ssl-valid-hostnames assoc :doc "Valid hostnames (hostname regex)")
                ~(vary-meta `rq-output-file assoc :doc "Request output file")
                ~(vary-meta `rs-output-file assoc :doc "Response output file")
                ~(vary-meta `output-stylesheet assoc :doc "Output stylesheet")
                ~(vary-meta `output-format assoc :doc "Output format [txt|xml|raw]")]
         :as all#}]
     (apply soma/execute ~stylesheet (interleave (keys all#) (vals all#)))))

(defsomacmd domain-quiesce "classpath:org/dpctl/stylesheets/soma/actions/domain-quiesce.xsl")

(defsomacmd domain-unquiesce "classpath:org/dpctl/stylesheets/soma/actions/domain-unquiesce.xsl")

(defsomacmd flush-document-cache "classpath:org/dpctl/stylesheets/soma/actions/flush-document-cache.xsl")

(defsomacmd flush-stylesheet-cache "classpath:org/dpctl/stylesheets/soma/actions/flush-stylesheet-cache.xsl")

(defsomacmd ping "classpath:org/dpctl/stylesheets/soma/actions/ping.xsl")

(defsomacmd remove-checkpoint "classpath:org/dpctl/stylesheets/soma/actions/remove-checkpoint.xsl")

(defsomacmd remove-dir "classpath:org/dpctl/stylesheets/soma/actions/remove-dir.xsl")

(defsomacmd reset-domain "classpath:org/dpctl/stylesheets/soma/actions/reset-domain.xsl")

(defsomacmd reset-this-domain "classpath:org/dpctl/stylesheets/soma/actions/reset-this-domain.xsl")

(defsomacmd restart-domain "classpath:org/dpctl/stylesheets/soma/actions/restart-domain.xsl")

(defsomacmd restart-this-domain "classpath:org/dpctl/stylesheets/soma/actions/restart-this-domain.xsl")

(defsomacmd rollback-checkpoint "classpath:org/dpctl/stylesheets/soma/actions/rollback-checkpoint.xsl")

(defsomacmd save-checkpoint "classpath:org/dpctl/stylesheets/soma/actions/save-checkpoint.xsl")

(defsomacmd save-config "classpath:org/dpctl/stylesheets/soma/actions/save-config.xsl")

(defsomacmd tcp-connection-test "classpath:org/dpctl/stylesheets/soma/actions/tcp-connection-test.xsl")

(defsomacmd do-export "classpath:org/dpctl/stylesheets/soma/do-export.xsl")

(defsomacmd do-import "classpath:org/dpctl/stylesheets/soma/do-import.xsl")

(defsomacmd do-view-certificate-details "classpath:org/dpctl/stylesheets/soma/do-view-certificate-details.xsl")

(defsomacmd get-config "classpath:org/dpctl/stylesheets/soma/get-config.xsl")

(defsomacmd get-conformance-report "classpath:org/dpctl/stylesheets/soma/get-conformance-report.xsl")

(defsomacmd get-diff "classpath:org/dpctl/stylesheets/soma/get-diff.xsl")

(defsomacmd get-file "classpath:org/dpctl/stylesheets/soma/get-file.xsl")

(defsomacmd get-filestore "classpath:org/dpctl/stylesheets/soma/get-filestore.xsl")

(defsomacmd get-log "classpath:org/dpctl/stylesheets/soma/get-log.xsl")

(defsomacmd get-status "classpath:org/dpctl/stylesheets/soma/get-status.xsl")

(defsomacmd set-file "classpath:org/dpctl/stylesheets/soma/set-file.xsl")
