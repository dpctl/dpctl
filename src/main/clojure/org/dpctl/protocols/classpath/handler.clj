(ns org.dpctl.protocols.classpath.handler
  (:require [clojure.java.io :as io]
            [org.dpctl.logger :as logger])
  (:import (java.net URL
                     URLConnection
                     URLStreamHandler))
  (:gen-class
   :name org.dpctl.protocols.classpath.Handler
   :extends java.net.URLStreamHandler))

(defn -openConnection
  [_ ^URL url]
  (logger/debug "Open: %s " (str url))
  (.openConnection (io/resource (.getPath url))))
