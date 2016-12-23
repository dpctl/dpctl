(ns org.dpctl.config-test
  (:require [org.dpctl.config :as config])
  (:use [clojure test]))

(use-fixtures :each
  (fn [f]
    (let [c (config/get-config)]
      (f)
      (config/set-config c))))

(deftest test-default-config
  (is (= {} (config/get-config))))

(deftest test-set-config
  (is (= {:key "value"} (config/set-config {:key "value"}))))
