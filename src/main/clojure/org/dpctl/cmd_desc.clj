(ns org.dpctl.cmd-desc
  (:require [clojure.tools.cli :as cli]
            [org.dpctl.logger :as logger]
            [org.dpctl.config :as config]
            [org.dpctl.constants :as constants]
            [org.dpctl.java.xml :as xml]
            [org.dpctl.util.soma :as soma]))

(def common-soma-parameters #{"dp-mgmt-url" "dp-user-name" "dp-user-password" "domain" "ssl-trust-all-certificates" "ssl-valid-hostnames" "rq-output-file" "rs-output-file" "output-stylesheet" "output-format"})

(defn cli-option
  [name type short-option doc default]
  (-> []
      (conj (if (some? short-option) (str "-" short-option) nil))
      (conj (format "--%s%s" name (if (= type "boolean") "" " <val>")))
      (conj doc)
      (conj :default default)))

(defn cli-options-from-metadata
  [m]
  (let [l (:arglists m)
        arg-list (first l)
        variadic-delimitor (first arg-list)
        variadic-args (first (next arg-list))
        keys (:keys variadic-args)
        defaults (:or variadic-args)
        c (config/get-config)]
    (assert (= 1 (count l)) "Command functions must be of arity 1")
    (assert (= "&" (str variadic-delimitor)) "The command function must be a variadic function")
    (assert (map? variadic-args) "The command function must use associative destructuring")

    (map #(let [an (name %)
                am (meta %)]
            (cli-option an
                        (:type am)
                        (:short-option am)
                        (:doc am)
                        (get c an ((keyword an) defaults))))
         keys)))

(defn cli-options-from-stylesheet
  [stylesheet]
  (let [p (soma/get-stylesheet-parameters stylesheet)
        c (config/get-config)]
    (logger/debug "Stylesheet parameters: %s" p)

    (map #(cli-option (:name %)
                      (:type %)
                      (:short-option %)
                      (:doc %)
                      (get c (:name %) (:default %)))
         (filter #(not (contains? common-soma-parameters (:name %))) p))))

(defn metadata-desc
  [f desc-params cmd-options]
  (let [m (meta f)]
    {:name (name (:name m))
     :category (:category m)
     :doc (:doc m)
     :cli-options (cli-options-from-metadata m)
     :fn f}))

(defn stylesheet-desc
  [f desc-params cmd-options]
  (let [m (meta f)
        stylesheet (:stylesheet desc-params)
        document (.parse (xml/document-builder) stylesheet)]
    {:name (name (:name m))
     :category (soma/get-soma-cmd-category document)
     :doc (soma/get-soma-cmd-documentation document)
     :cli-options (concat (cli-options-from-metadata m) (cli-options-from-stylesheet document))
     :fn f}))
