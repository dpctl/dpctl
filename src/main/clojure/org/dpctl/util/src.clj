(ns org.dpctl.util.src
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.codec.base64 :as base64]
            [clojure.data.json :as json]
            [org.dpctl.logger :as logger]
            [org.dpctl.java.xml :as xml])
  (:import (java.io FileOutputStream)
           (java.security MessageDigest)
           (javax.xml.xpath XPathConstants)))

(defn direct-inherited-src-dirs
  [dir]
  (let [f (io/file dir "config.json")]
    (when (.exists f)
      (let [cfg (json/read-str (slurp f) :key-fn keyword)
            extends (:extends cfg)]
        (map #(let [f (io/file %)]
                (if (.isAbsolute f)
                  (.getCanonicalPath f)
                  (.getCanonicalPath (io/file dir f))))
             extends)))))

(defn src-dirs
  [dir inheritance-depth]
  (when (> inheritance-depth 0)
    (let [d (.getCanonicalFile (io/file dir))]
      (reduce (fn [x c] (if (not-any? #(= c %) x)
                          (vec (distinct (concat x (src-dirs c (dec inheritance-depth)))))
                          x))
              (vector (.getCanonicalPath d))
              (direct-inherited-src-dirs d)))))

(defn configuration-document
  [domain version]
  (let [doc (xml/new-document)
        dp-config-el (.createElement doc "datapower-configuration")
        config-el (.createElement doc "configuration")
        files-el (.createElement doc "files")]
    (.setAttribute dp-config-el "version" version)
    (.setAttribute config-el "domain" domain)
    (.appendChild doc dp-config-el)
    (.appendChild dp-config-el config-el)
    (.appendChild dp-config-el files-el)
    doc))

(defn get-sorted-object-docs
  "Sorts the objects so the dependent objects are in front of the object."
  [obj-doc-map obj-refs-map objects]
  (let [res (reduce (fn [v o]
                      (let [doc (obj-doc-map o)
                            refs (obj-refs-map o)]
                        (vec (-> v
                                 (concat (get-sorted-object-docs obj-doc-map obj-refs-map refs))
                                 (concat (if (some? doc) [doc] []))))))
                    []
                    objects)]
    (distinct res)))

(defn get-object-refs
  [node-set]
  (let [xpath-expr (.compile (xml/new-xpath) "./text()")
        refs (for [i (range (.getLength node-set))
                   :let [node (.item node-set i)
                         class (.getAttribute node "class")
                         name (.evaluate xpath-expr node)]]
               [class name])]
    (into [] refs)))

(defn get-objects
  [src-dir]
  (let [objects-path (io/file src-dir "objects")
        files (filter #(and (.isFile %)
                            (not (.isHidden %)))
                      (file-seq objects-path))]
    (reduce #(let [name (.getName %2)
                   class (.getName (.getParentFile %2))]
               (assert (str/ends-with? name ".xml") (format "Invalid object file: '%s'" name))
               (assert (.equals objects-path (.getParentFile (.getParentFile %2))) (format "Invalid object file location: '%s'" (.getCanonicalPath %2)))
               (assoc %1 [class (subs name 0 (- (count name) 4))] (.getCanonicalPath %2))) {} files)))

(defn get-object-nodes
  [src-dirs]
  (let [obj-path-map (reduce #(merge %1 (get-objects %2)) {} (reverse src-dirs))
        document-builder (xml/document-builder)
        obj-doc-map (reduce-kv (fn [m k v]
                                 (let [o (.parse document-builder (io/file v))
                                       dpoe (.getDocumentElement o)
                                       class (.getLocalName dpoe)
                                       name (.getAttribute dpoe "name")]
                                   (assert (.equals class (first k)) (format "Object directory (%s) and object class (%s) does not match" (first k) name))
                                   (assert (.equals name (second k)) (format "Object file (%s) and object name (%s) does not match" (second k) name))
                                   (assoc m k o)))
                               {}
                               obj-path-map)
        xpath-expr (.compile (xml/new-xpath) "//*[@class]")
        obj-refs-map (reduce #(let [[cn o] %2
                                    ns (.evaluate xpath-expr o XPathConstants/NODESET)]
                                (assoc %1 cn (get-object-refs ns)))
                             (sorted-map)
                             (seq obj-doc-map))]
    (get-sorted-object-docs obj-doc-map obj-refs-map (keys obj-refs-map))))

(defn add-dp-objects
  [src-dirs include exclude configuration-document]
  (let [configuration-element (.evaluate (xml/new-xpath) "/datapower-configuration/configuration" configuration-document XPathConstants/NODE)
        dp-objects (get-object-nodes src-dirs)]
    (doseq [dp-object dp-objects
            :let [dpoe (.getDocumentElement dp-object)
                  class (.getLocalName dpoe)
                  name (.getAttribute dpoe "name")
                  p (format "%s/%s.xml" class name)]]
      (when (and (not (empty? include))
                 (re-matches (re-pattern include) p)
                 (or (empty? exclude)
                     (not (re-matches (re-pattern exclude) p))))
        (.appendChild configuration-element (.importNode configuration-document dpoe true))))))

(defn calculate-hash
  [data]
  (let [md (MessageDigest/getInstance "SHA-1")
        d (.digest md (.getBytes data))]
    (new String (base64/encode d))))

(defn get-files
  [src-dir]
  (let [files-path (io/file src-dir "files")
        files (filter #(and (.isFile %)
                            (not (.isHidden %)))
                      (file-seq files-path))]
    (reduce #(let [fcp (.getCanonicalPath %2)
                   rcp (.getCanonicalPath files-path)]
               (assert (str/starts-with? fcp rcp) (format "Invalid file location: '%s'"fcp))
               (assoc %1 (subs fcp (+ (count rcp) 1)) (.getCanonicalPath %2))) {} files)))

(defn get-file-nodes
  [src-dirs]
  (let [document-builder (xml/document-builder)
        files-path-map (reduce #(merge %1 (get-files %2)) {} (reverse src-dirs))]
    (reduce-kv (fn [m k v]
                 (let [d (slurp v)
                       f (new String (base64/encode (.getBytes d)))
                       h (calculate-hash d)
                       doc (.newDocument document-builder)
                       fe (.createElement doc "file")
                       uk (str/replace k #"\\" "/")]
                   (.appendChild doc fe)
                   (.setAttribute fe "name" (str/replace-first uk #"/" ":///"))
                   (.setAttribute fe "hash" h)
                   (.setAttribute fe "src" uk)
                   (.appendChild fe (.createTextNode doc f))
                   (conj m doc))) [] files-path-map)))

(defn add-dp-files
  [src-dirs include exclude configuration-document]
  (let [files-element (.evaluate (xml/new-xpath) "/datapower-configuration/files" configuration-document XPathConstants/NODE)
        dp-files (get-file-nodes src-dirs)]
    (doseq [dp-file dp-files
            :let [dpfe (.getDocumentElement dp-file)
                  name (.getAttribute dpfe "name")]]
      (when (and (not (empty? include))
                 (re-matches (re-pattern include) name)
                 (or (empty? exclude)
                     (not (re-matches (re-pattern exclude) name))))
        (.appendChild files-element (.importNode configuration-document dpfe true))))))

(defn dump-xml
  [doc]
  (logger/info "%s" (.transform (xml/new-transformer) (xml/new-dom-source doc) (xml/new-stream-result *out*))))

(defn build-configuration
  [domain version dir inheritance-depth include-objects include-files exclude-objects exclude-files]
  (let [document (configuration-document domain version)
        src-dirs (src-dirs dir inheritance-depth)]
    (logger/debug "Source directories: %s" (str/join " -> " src-dirs))
    (add-dp-objects src-dirs include-objects exclude-objects document)
    (add-dp-files src-dirs include-files exclude-files document)
    document))

(defn get-object-local-path
  "The path to the first object found in the source directory."
  [src-dirs class name]
  (let [p (format "%s/%s.xml" class name)]
    (let [src-dir (->> src-dirs
                       (filter #(let [f (io/file % "objects" p)]
                                  (.exists f)))
                       first)]
      (if (some? src-dir)
        (.getCanonicalPath (io/file src-dir "objects" p))
        (.getCanonicalPath (io/file (first src-dirs) "objects" p))))))

(defn get-file-local-path
  "The path to the first file found in the source directory."
  [src-dirs name]
  (let [p (str/replace-first name #":///" "/")]
    (let [src-dir (->> src-dirs
                       (filter #(let [f (io/file % "files" p)]
                                  (.exists f)))
                       first)]

      (if (some? src-dir)
        (.getCanonicalPath (io/file src-dir "files" p))
        (.getCanonicalPath (io/file (first src-dirs) "files" p))))))

(defn save-dp-objects
  [src-dirs include exclude configuration-document]
  (let [templates (xml/new-templates (xml/new-stream-source "classpath:org/dpctl/stylesheets/util/pull-object-handler.xsl"))
        node-set (.evaluate (xml/new-xpath) "/datapower-configuration/configuration/*" configuration-document XPathConstants/NODESET)]
    (doseq [i (range (.getLength node-set))
            :let [node (.item node-set i)
                  class (.getLocalName node)
                  name (.getAttribute node "name")
                  p (format "%s/%s" class name)]]
      (when (and (not (empty? include))
                 (re-matches (re-pattern include) p)
                 (or (empty? exclude)
                     (not (re-matches (re-pattern exclude) p))))
        (let [path (get-object-local-path src-dirs class name)]
          (.mkdirs (.getParentFile (io/file path)))
          (.transform (.newTransformer templates) (xml/new-dom-source node) (xml/new-stream-result path))
          (logger/info "Object saved: %s" path))))))

(defn save-dp-files
  [src-dirs include exclude configuration-document]
  (let [xpath-expr (.compile (xml/new-xpath) "./text()")
        node-set (.evaluate (xml/new-xpath) "/datapower-configuration/files/file" configuration-document XPathConstants/NODESET)]
    (doseq [i (range (.getLength node-set))
            :let [node (.item node-set i)
                  name (.getAttribute node "name")
                  p (str/replace-first name #":///" "/")]]
      (when (and (not (empty? include))
                 (re-matches (re-pattern include) p)
                 (or (empty? exclude)
                     (not (re-matches (re-pattern exclude) p))))
        (let [path (get-file-local-path src-dirs name)]
          (.mkdirs (.getParentFile (io/file path)))
          (with-open [o (new FileOutputStream path)]
            (.write o (base64/decode (.getBytes (.evaluate xpath-expr node))))
            (logger/info "File saved: %s" path)))))))

(defn save-configuration
  [document dir inheritance-depth include-objects include-files exclude-objects exclude-files]
  (let [src-dirs (src-dirs dir inheritance-depth)]
    (logger/debug "Source directories: %s" (str/join " -> " src-dirs))
    (save-dp-objects src-dirs include-objects exclude-objects document)
    (save-dp-files src-dirs include-files exclude-files document)))
