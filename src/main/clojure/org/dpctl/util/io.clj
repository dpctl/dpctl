(ns org.dpctl.util.io
  (:require [clojure.java.io :as io]
            [clojure.data.codec.base64 :as base64]
            [org.dpctl.logger :as logger])
  (:import (java.io ByteArrayInputStream
                    ByteArrayOutputStream)))

(defn base64-decode-data-to-file
  "Decodes and save base64 encoded data."
  [file base64-content]
  (with-open [in (ByteArrayInputStream. (.getBytes base64-content))
              out (io/output-stream file)]
    (base64/decoding-transfer in out)))

(defn base64-encode-data-from-file
  "Decodes and save base64 encoded data."
  [file]
  (with-open [in (io/input-stream file)
              out (new ByteArrayOutputStream)]
    (base64/encoding-transfer in out)
    (.toString out "UTF-8")))

(defn save-data-to-file
  "Save data from file"
  [name content base64-encoded]
  (let [file (io/file name)]
    (io/make-parents file)
    (if base64-encoded
      (base64-decode-data-to-file file content)
      (spit file content))
    (logger/debug "File saved: %s" (.getCanonicalPath file))))

(defn read-data-from-file
  "Read data from file"
  [name base64-encoded]
  (let [file (io/file name)
        content (if base64-encoded
                  (base64-encode-data-from-file file)
                  (slurp file))]
    (logger/debug "File read: %s" (.getCanonicalPath file))
    content))
