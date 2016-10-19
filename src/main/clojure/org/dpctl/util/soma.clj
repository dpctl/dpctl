(ns org.dpctl.util.soma
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [org.dpctl.constants :as constants]
            [org.dpctl.logger :as logger]
            [org.dpctl.java.xml :as xml]
            [org.dpctl.java.net :as net]
            [org.dpctl.java.ssl :as ssl])
  (:import (java.io ByteArrayInputStream
                    ByteArrayOutputStream
                    FileOutputStream)
           (org.w3c.dom Node)))

(defn get-parameter-nodes
  "Returns the parameter child nodes"
  [node]
  (let [namespace (.getNamespaceURI node)]
    (loop [node (.getFirstChild node)
           result []]
      (cond
        (nil? node) result
        (and (= Node/ELEMENT_NODE (.getNodeType node))
             (= "param" (.getLocalName node))
             (= namespace (.getNamespaceURI node))) (recur (.getNextSibling node) (conj result node))
        :else (recur (.getNextSibling node) result)))))

(defn get-stylesheet-parameters
  "Extracts the stylesheet parameters."
  [document]
  (let [stylesheet-node (.getDocumentElement document)
        param-nodes (get-parameter-nodes stylesheet-node)]
    (reduce
     #(let [sn (.getAttributeNode %2 "select")
            p (-> {}
                  (assoc :name (.getAttribute %2 "name"))
                  (assoc :type (.getAttributeNS %2 constants/dpctl-xml-namespace "type"))
                  (assoc :default (.getAttributeNS %2 constants/dpctl-xml-namespace "default"))
                  (assoc :doc (.getAttributeNS %2 constants/dpctl-xml-namespace "doc")))]
        (conj %1 p))
     [] param-nodes)))

(defn get-soma-cmd-documentation
  "Returns the SOMA command documentation."
  [document]
  (let [stylesheet-node (.getDocumentElement document)]
    (.getAttributeNS stylesheet-node constants/dpctl-xml-namespace "doc")))

(defn get-soma-cmd-category
  "Returns the SOMA command category."
  [document]
  (let [stylesheet-node (.getDocumentElement document)]
    (.getAttributeNS stylesheet-node constants/dpctl-xml-namespace "category")))

(defn get-templates
  [stylesheet]

  (let [source (xml/new-stream-source stylesheet)]
    (.setSystemId source stylesheet)
    (xml/new-templates source)))

(defn set-transform-parameters
  "Set the parameters for the transformation."
  [transformer parameters]
  (doseq [[k v] parameters
          :when (some? v)]
    (.setParameter transformer (name k) v)))

(defn write-to-file
  [file data]
  (when (not (str/blank? file))
    (with-open [o (new FileOutputStream file)]
      (.write o data))
    (logger/debug "Data saved to file: %s" file)))

(defn execute
  [stylesheet & {:keys [dp-mgmt-url
                        dp-user-name
                        dp-user-password
                        domain
                        ssl-trusted-certificates
                        ssl-valid-hostnames
                        rq-output-file
                        rs-output-file
                        output-stylesheet
                        output-format]
                 :as all}]
  (ssl/init-https-connection ssl-trusted-certificates ssl-valid-hostnames)

  (let [rq-transformer (.newTransformer (get-templates stylesheet))
        rq-output-stream (new ByteArrayOutputStream)]
    (set-transform-parameters rq-transformer all)
    (.transform rq-transformer (xml/new-dom-source (xml/new-document)) (xml/new-stream-result rq-output-stream))

    (write-to-file rq-output-file (.toByteArray rq-output-stream))

    (let [rs-data (net/http-post dp-mgmt-url dp-user-name dp-user-password (.toByteArray rq-output-stream))]

      (write-to-file rs-output-file rs-data)

      (when (not (empty? output-stylesheet))
        (let [rs-transformer (.newTransformer (get-templates output-stylesheet))
              rs-input-stream (new ByteArrayInputStream rs-data)]
          (set-transform-parameters rs-transformer all)
          (.transform rs-transformer (xml/new-stream-source rs-input-stream) (xml/new-stream-result *out*))))
      rs-data)))
