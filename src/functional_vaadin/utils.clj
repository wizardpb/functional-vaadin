(ns functional-vaadin.utils
  "Generally useful utility functions"
  (:require [clojure.string :as str])
  (:import [com.vaadin.ui AbstractComponent]))

(defn capitalize [s]
  (if (empty? s) s (str (.toUpperCase (subs s 0 1)) (subs s 1))))

(defn uncapitalize [s]
  (if (empty? s) s (str (.toLowerCase (subs s 0 1)) (subs s 1))))

(defn attach-data
  "Attach data to a Component indexed by a key. The data is stored in a Map under the key, which is in turn
  stored in the setData() attribute of the Component"
  [component key data]
  (.setData component
            (if-let [m (.getData component)]
              (assoc m key data)
              (hash-map key data)))
  )

(defn get-data
  "Get any attached data at key"
  [component key]
  (get (.getData component) key))

(defn detach-data
  "Get and remove any attached data at key"
  [component key]
  (let [m (.getData component)]
    (.setData component (dissoc m key))
    (get m key)))

(defn humanize
  "Turn a keyword or symbol string into a humanized for. The text is split at hyphens (-) and each segment is capitalized"
  [kw-or-string]
  (str/join " " (map capitalize (str/split (name kw-or-string) #"-"))))