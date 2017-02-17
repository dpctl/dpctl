(ns org.dpctl.java.ssl-test
  (:require [clojure.java.io :as io]
            [org.dpctl.java.ssl :as ssl])
  (:use [clojure test])
  (:import (javax.net.ssl SSLContext
                          SSLSession
                          HttpsURLConnection
                          X509TrustManager
                          HostnameVerifier)
           (java.security.cert CertificateFactory
                               X509Certificate)))

(defn x509-certificate
  "SSL Certificate factory."
  []
  (.generateCertificate (CertificateFactory/getInstance "X.509")
                        (io/input-stream (io/resource "org/dpctl/test.dpctl.org.pem"))))

(defn reify-ssl-session
  "SSL Session factory."
  [opts]
  (let [{peer-host :peer-host} opts]
    (reify SSLSession
      (getPeerHost [this] peer-host))))

(deftest test-cert-fingerprint
  (is (= "74:8A:52:6C:DA:2F:02:40:1D:00:D4:0D:7F:B8:E8:82:AB:27:E9:81"
         (ssl/cert-fingerprint (x509-certificate)))))

(deftest test-trust-manager
  (is (nil? (.checkClientTrusted (ssl/reify-trust-manager nil) nil nil)))
  (is (nil? (.checkClientTrusted (ssl/reify-trust-manager "") nil nil)))
  (is (nil? (.checkClientTrusted (ssl/reify-trust-manager ".*") nil nil)))
  (is (nil? (.getAcceptedIssuers (ssl/reify-trust-manager nil))))
  (is (nil? (.getAcceptedIssuers (ssl/reify-trust-manager ""))))
  (is (nil? (.getAcceptedIssuers (ssl/reify-trust-manager ".*"))))
  (is (thrown-with-msg? java.security.cert.CertificateException
                        #"Server certificates not trusted"
                        (.checkServerTrusted (ssl/reify-trust-manager nil)
                                             nil nil)))
  (is (thrown-with-msg? java.security.cert.CertificateException
                        #"Server certificates not trusted"
                        (.checkServerTrusted (ssl/reify-trust-manager "")
                                             nil nil)))
  (is (thrown-with-msg? java.security.cert.CertificateException
                        #"Server certificates not trusted"
                        (.checkServerTrusted (ssl/reify-trust-manager ".*")
                                             nil nil)))
  (is (thrown-with-msg? java.security.cert.CertificateException
                        #"Server certificates not trusted"
                        (.checkServerTrusted (ssl/reify-trust-manager nil)
                                             (into-array X509Certificate (vector (x509-certificate))) nil)))
  (is (thrown-with-msg? java.security.cert.CertificateException
                        #"Server certificates not trusted"
                        (.checkServerTrusted (ssl/reify-trust-manager "")
                                             (into-array X509Certificate (vector (x509-certificate))) nil)))
  (is (thrown-with-msg? java.security.cert.CertificateException
                        #"Server certificates not trusted"
                        (.checkServerTrusted (ssl/reify-trust-manager "FI:NG:ER:PR:IN:T")
                                             (into-array X509Certificate (vector (x509-certificate))) nil)))
  (is (nil? (.checkServerTrusted (ssl/reify-trust-manager ".*")
                                 (into-array X509Certificate (vector (x509-certificate))) nil)))
  (is (nil? (.checkServerTrusted (ssl/reify-trust-manager "^74:8A:.*:E9:81$")
                                 (into-array X509Certificate (vector (x509-certificate))) nil))))

(deftest test-hostname-verifier
  (let [ssl-session (reify-ssl-session {:peer-host "dpmgmt.domain"})]
    (is (= true (.verify (ssl/reify-hostname-verifier nil) "dpmgmt.domain" ssl-session)))
    (is (= true (.verify (ssl/reify-hostname-verifier "") "dpmgmt.domain" ssl-session)))
    (is (= true (.verify (ssl/reify-hostname-verifier nil) "DPMGMT.DOMAIN" ssl-session)))
    (is (= true (.verify (ssl/reify-hostname-verifier "") "DPMGMT.DOMAIN" ssl-session)))
    (is (= false (.verify (ssl/reify-hostname-verifier nil) "dpmgmt2.domain" ssl-session)))
    (is (= false (.verify (ssl/reify-hostname-verifier "") "dpmgmt2.domain" ssl-session)))
    (is (= true (.verify (ssl/reify-hostname-verifier "dpmgmt[.]domain") "ignored" ssl-session)))
    (is (= true (.verify (ssl/reify-hostname-verifier "dpmgmt[.]domain") "ignored" ssl-session)))
    (is (= true (.verify (ssl/reify-hostname-verifier "(?i)DPMGMT[.]DOMAIN") "ignored" ssl-session)))
    (is (= true (.verify (ssl/reify-hostname-verifier "dp.*domain") "ignored" ssl-session)))
    (is (= false (.verify (ssl/reify-hostname-verifier "dpmgmt2[.]domain") "dpmgmt.domain" ssl-session)))
    (is (= false (.verify (ssl/reify-hostname-verifier "dpmgmt[.]domain2") "dpmgmt.domain" ssl-session)))))

(deftest test-init-https-connection
  (ssl/init-https-connection nil nil)
  (is (instance? javax.net.ssl.HttpsURLConnection$DefaultHostnameVerifier
                 (HttpsURLConnection/getDefaultHostnameVerifier)))

  (ssl/init-https-connection "" "")
  (is (instance? javax.net.ssl.HttpsURLConnection$DefaultHostnameVerifier
                 (HttpsURLConnection/getDefaultHostnameVerifier)))

  (ssl/init-https-connection ".*" ".*")
  (is (re-matches #"org.dpctl.java.ssl\$reify_hostname_verifier.*"
                  (.getName (class (HttpsURLConnection/getDefaultHostnameVerifier))))))
