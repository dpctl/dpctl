(ns org.dpctl.java.xml
  (:require [org.dpctl.constants :as constants])
  (:import (javax.xml XMLConstants)
           (javax.xml.namespace NamespaceContext)
           (javax.xml.parsers DocumentBuilderFactory)
           (javax.xml.validation SchemaFactory)
           (javax.xml.transform TransformerFactory)
           (javax.xml.transform.dom DOMSource
                                    DOMResult)
           (javax.xml.transform.stream StreamSource
                                       StreamResult)
           (javax.xml.soap SOAPConstants)
           (javax.xml.xpath XPathFactory
                            XPathConstants)))

(defn reify-namespace-context
  "NamespaceContext factory."
  []
  (reify NamespaceContext
    (getNamespaceURI [this prefix]
      (cond
        (nil? prefix) (throw (IllegalArgumentException. "No namespace prefix provided"))
        (= prefix "dpctl") constants/dpctl-xml-namespace
        (= prefix "mgmt") constants/dp-mgmt-xml-namespace
        (= prefix "soap11") SOAPConstants/URI_NS_SOAP_1_1_ENVELOPE
        (= prefix "soap12") SOAPConstants/URI_NS_SOAP_1_2_ENVELOPE
        :else XMLConstants/NULL_NS_URI))
    (getPrefix [this namespaceUri] nil)
    (getPrefixes [this namespaceUri] nil)))

(defn document-builder-factory
  "Obtain a new instance of a DocumentBuilderFactory."
  [& {:keys [namespace-aware coalescing ignore-whitespace]}]
  (let [factory (DocumentBuilderFactory/newInstance)]
    (when (some? namespace-aware)
      (.setNamespaceAware factory (true? namespace-aware)))
    (when (some? coalescing)
      (.setCoalescing factory (true? coalescing)))
    (when (some? ignore-whitespace)
      (.setIgnoringElementContentWhitespace factory (true? ignore-whitespace)))
    factory))

(defn document-builder
  "Creates a new instance of a DocumentBuilder."
  []
  (.newDocumentBuilder (document-builder-factory :namespace-aware true)))

(defn new-document
  "Obtain a new instance of a DOM Document."
  []
  (.newDocument (document-builder)))

(defn transformer-factory
  "Obtain a new instance of a TransformerFactory."
  [& opts]
  (let [{uri-resolver :uri-resolver error-listener :error-listener features :features attributes :attributes} opts
        factory (TransformerFactory/newInstance)]
    (when uri-resolver
      (.setURIResolver factory uri-resolver))
    (when error-listener
      (.setErrorListener factory error-listener))
    (doseq [[name val] features]
      (.setFeature factory name val))
    (doseq [[name val] attributes]
      (.setAttribute factory name val))
    factory))

(defn new-transformer
  "Create a new Transformer"
  ([]
   (.newTransformer (transformer-factory)))
  ([source]
   (.newTransformer (transformer-factory) source)))

(defn new-templates
  "Create a new Templates"
  [source]
  (.newTemplates (transformer-factory) source))

(defn new-xpath
  "Create a new XPath"
  [& {:keys [namespace-context function-resolver variable-resolver]}]
  (let [xpath (.newXPath (XPathFactory/newInstance))]
    (when (some? namespace-context)
      (.setNamespaceContext xpath namespace-context))
    (when (some? function-resolver)
      (.setXPathFunctionResolver xpath function-resolver))
    (when (some? variable-resolver)
      (.setXPathVariableResolver xpath variable-resolver))
    xpath))

(defn new-dom-source
  [node]
  (if (nil? node)
    (new DOMSource)
    (new DOMSource node)))

(defn new-dom-result
  [node]
  (if (nil? node)
    (new DOMResult)
    (new DOMResult node)))

(defn new-stream-source
  [in]
  (if (nil? in)
    (new StreamSource)
    (new StreamSource in)))

(defn new-stream-result
  [out]
  (if (nil? out)
    (new StreamResult)
    (new StreamResult out)))

(defn set-transform-parameters
  "Set the parameters for the transformation."
  [transformer parameters]
  (reduce-kv (fn [t k v]
               (.setParameter t k v)
               t) transformer parameters))
