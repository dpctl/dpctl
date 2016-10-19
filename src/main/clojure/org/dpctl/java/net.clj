(ns org.dpctl.java.net
  (:require [clojure.java.io :as io]
            [clojure.data.codec.base64 :as base64]
            [org.dpctl.constants :as constants]
            [org.dpctl.logger :as logger])
  (:import (java.io ByteArrayOutputStream)
           (java.net URL)))

(defn authorization-header
  [username password]
  (let [up (format "%s:%s" username password)
        upe (String. (base64/encode (bytes (byte-array (map byte up)))))]
    (format "Basic %s" upe)))

(defn http-post
  "Posts a request file to "
  [url username password rq-data]
  (let [start-time (System/nanoTime)
        connection (.openConnection (new URL url))]
    (.setRequestMethod connection constants/http-method-post)
    (.setRequestProperty connection constants/http-header-content-type constants/media-type-text-xml)
    (.setUseCaches connection false)
    (.setDoOutput connection true)
    (.setDoInput connection true)
    (.setRequestProperty connection "Authorization" (authorization-header username password))
    (.connect connection)
    (let [output (.getOutputStream connection)]
      (.write output rq-data)
      (.close output))

    (logger/debug "HTTP response status: %s (%d)"
                  (.getResponseMessage connection)
                  (.getResponseCode connection))

    (let [input (if (< (.getResponseCode connection) 400)
                  (.getInputStream connection)
                  (.getErrorStream connection))
          output-stream (new ByteArrayOutputStream)]
      (io/copy input output-stream)
      (.close input)

      (logger/debug "DataPower call time: %.3f sec"
                    (/ (double (- (System/nanoTime) start-time)) 1000000000.0))
      (.toByteArray output-stream)
      )))
