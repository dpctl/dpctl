(ns org.dpctl.java.ssl
  (:require [clojure.string :as str]
            [org.dpctl.logger :as logger]
            [clojure.string :as str])
  (:import (javax.net.ssl SSLContext
                          HttpsURLConnection
                          X509TrustManager
                          HostnameVerifier)
           (java.security MessageDigest)
           (java.security.cert CertificateException)))

(defn cert-fingerprint
  "SHA-1 certificate fingerpint"
  [cert]
  (let [md (MessageDigest/getInstance "SHA-1")]
    (.update md (.getEncoded cert))
    (str/join ":" (map #(format "%02X" %) (seq (.digest md))))))

(defn reify-trust-manager
  "SSL TrustManager factory."
  [trusted-certificates]
  (reify X509TrustManager
    (checkClientTrusted [this chain auth-type] nil)
    (checkServerTrusted [this chain auth-type]
      (logger/debug "SSL trust manager. Trusted certificates: '%s'" trusted-certificates)

      (with-local-vars [trusted false]
        (doseq [cert (seq chain)
                :while (not @trusted)
                :let [fingerprint (cert-fingerprint cert)]]
          (logger/debug "Certificate subject: '%s', fingerprint: '%s'" (.getName (.getSubjectX500Principal cert)) fingerprint)

          (if (not (empty? trusted-certificates))
            (var-set trusted (some? (re-matches (re-pattern trusted-certificates) fingerprint)))))
        (when (not @trusted)
          (throw (new CertificateException "Server certificates not trusted.")))))
    (getAcceptedIssuers [this] nil)))

(defn reify-hostname-verifier
  "SSL HostnameVerifier factory."
  [valid-hostnames]
  (reify HostnameVerifier
    (verify [this hostname session]
      (do
        (logger/debug "SSL hostname verifier. Host: '%s', Peer: '%s', Valid hostnames: '%s'." hostname (.getPeerHost session) valid-hostnames)

        (if (empty? valid-hostnames)
          (.equalsIgnoreCase hostname (.getPeerHost session))
          (some? (re-matches (re-pattern valid-hostnames) (.getPeerHost session))))))))

(defn init-https-connection
  "Initialize the SSL context."
  [trusted-certificates valid-hostnames]
  (when (not (empty? trusted-certificates))
    (let [ssl-context (SSLContext/getInstance "SSL")
          trust-manager (reify-trust-manager trusted-certificates)]
      (.init ssl-context nil (into-array [trust-manager]) nil)
      (HttpsURLConnection/setDefaultSSLSocketFactory (.getSocketFactory ssl-context))))

  (when (not (empty? valid-hostnames))
    (let [hostname-verifier (reify-hostname-verifier valid-hostnames)]
      (HttpsURLConnection/setDefaultHostnameVerifier hostname-verifier))))
