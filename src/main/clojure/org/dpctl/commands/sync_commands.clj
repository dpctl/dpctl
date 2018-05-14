(ns org.dpctl.commands.sync-commands
  (:require [clojure.string :as str]
            [clojure.data.codec.base64 :as base64]
            [org.dpctl.logger :as logger]
            [org.dpctl.constants :as constants]
            [org.dpctl.cmd-desc :as desc]
            [org.dpctl.java.xml :as xml]
            [org.dpctl.util.soma :as soma]
            [org.dpctl.util.src :as src])
  (:import (java.io ByteArrayInputStream
                    ByteArrayOutputStream)
           (javax.xml.xpath XPathConstants)))

(defn ^{:cmd-desc-fn desc/metadata-desc
        :cmd-desc-params {}
        :category "Sync"} pull
  "Retrieve DataPower configuration artifacts"
  [& {:keys [^{:doc "DataPower management url" :short-option "u"} dp-mgmt-url
             ^{:doc "DataPower user name" :short-option "n"} dp-user-name
             ^{:doc "DataPower user password" :short-option "p"} dp-user-password
             ^{:doc "DataPower domain" :short-option "d"} domain
             ^{:doc "HTTP connect timeout" :short-option "c"} http-connect-timeout
             ^{:doc "HTTP read timeout" :short-option "r"} http-read-timeout
             ^{:doc "Trusted SSL certificates (fingerprint regex)"} ssl-trusted-certificates
             ^{:doc "Valid hostnames (hostname regex)"} ssl-valid-hostnames
             ^{:doc "Request output file"} rq-output-file
             ^{:doc "Response output file"} rs-output-file
             ^{:doc "Output stylesheet"} output-stylesheet
             ^{:doc "Output format [txt|xml|raw]"} output-format
             ^{:doc "Error check string (regex)"} error-check
             ^{:doc "Pull only persisted objects [true|false]"} persisted
             ^{:doc "Local source directory"} src-dir
             ^{:doc "Include objects matching pattern (regex)"} include-objects
             ^{:doc "Include files matching pattern (regex)"} include-files
             ^{:doc "Exclude objects matching pattern (regex)"} exclude-objects
             ^{:doc "Exclude files matching pattern (regex)"} exclude-files]
      :or {:persisted "false"}
      :as all}]
  (let [export-params (assoc all :format "XML" :all-files "true" :class "all-classes" :name "all-objects" :ref-objects "true" :ref-files "true" :include-debug "true")]
    (let [export-data (apply soma/execute "classpath:org/dpctl/stylesheets/soma/do-export.xsl" (interleave (keys export-params) (vals export-params)))
          export-data-doc (.parse (xml/document-builder) (new ByteArrayInputStream export-data))
          xpath (xml/new-xpath :namespace-context (xml/reify-namespace-context))
          config (.parse (xml/document-builder) (new ByteArrayInputStream (base64/decode (.getBytes (.evaluate xpath "/soap11:Envelope/soap11:Body/mgmt:response/mgmt:file" export-data-doc)))))]
      (.removeChild export-data-doc (.getDocumentElement export-data-doc))
      (src/save-configuration config src-dir constants/max-src-inheritance-depth include-objects include-files exclude-objects exclude-files))))

(defn ^{:cmd-desc-fn desc/metadata-desc
        :cmd-desc-params {}
        :category "Sync"} push
  "Update DataPower configuration artifacts"
  [& {:keys [^{:doc "DataPower management url" :short-option "u"} dp-mgmt-url
             ^{:doc "DataPower user name" :short-option "n"} dp-user-name
             ^{:doc "DataPower user password" :short-option "p"} dp-user-password
             ^{:doc "DataPower domain" :short-option "d"} domain
             ^{:doc "HTTP connect timeout" :short-option "c"} http-connect-timeout
             ^{:doc "HTTP read timeout" :short-option "r"} http-read-timeout
             ^{:doc "Trusted SSL certificates (fingerprint regex)"} ssl-trusted-certificates
             ^{:doc "Valid hostnames (hostname regex)"} ssl-valid-hostnames
             ^{:doc "Request output file"} rq-output-file
             ^{:doc "Response output file"} rs-output-file
             ^{:doc "Output stylesheet"} output-stylesheet
             ^{:doc "Output format [txt|xml|raw]"} output-format
             ^{:doc "Error check string (regex)"} error-check
             ^{:doc "Local source directory"} src-dir
             ^{:doc "DataPower configuration version"} dp-configuration-version
             ^{:doc "Include objects matching pattern (regex)"} include-objects
             ^{:doc "Include files matching pattern (regex)"} include-files
             ^{:doc "Exclude objects matching pattern (regex)"} exclude-objects
             ^{:doc "Exclude files matching pattern (regex)"} exclude-files]
      :as all}]
  (let [config (src/build-configuration domain dp-configuration-version src-dir constants/max-src-inheritance-depth include-objects include-files exclude-objects exclude-files)
        out (new ByteArrayOutputStream)]
    (.transform (xml/new-transformer) (xml/new-dom-source config) (xml/new-stream-result out))
    (.removeChild config (.getDocumentElement config))
    (let [import-params (assoc all :format "XML" :dry-run "false" :overwrite-objects "true" :overwrite-files "true" :rewrite-local-ip "true" :import-input (new String (base64/encode (.toByteArray out))))]
      (.reset out)
      (apply soma/execute "classpath:org/dpctl/stylesheets/soma/do-import.xsl" (interleave (keys import-params) (vals import-params))))))
