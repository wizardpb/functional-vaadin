(ns functional-vaadin.utils
  "Generally useful utility functions"
  (:require [clojure.string :as str])
  (:import (com.vaadin.ui AbstractComponent)))

(defn capitalize [s]
  (if (empty? s) s (str (.toUpperCase (subs s 0 1)) (subs s 1))))

(defn uncapitalize [s]
  (if (empty? s) s (str (.toLowerCase (subs s 0 1)) (subs s 1))))

(defn parse-key [key]
  (cond
    (string? key) (map keyword (str/split key #"\."))
    (keyword? key) (map keyword (str/split (name key) #"\."))
    (or (seq? key) (vector? key)) (map keyword key)
    )
  )

(defn component-key [id]
  (concat [:components] (parse-key id)))

(defn binding-key [id]
  (concat [:bindings] (parse-key id)))

(defn attach-data
  "Attach data to a Component indexed by a key. The data is stored in a Map under the key, which is in turn
  stored in the setData() attribute of the Component"
  [^AbstractComponent component key data]
  (.setData component (assoc-in (.getData component) (parse-key key) data)))

(defn get-data
  "Get any attached data at key"
  [component key]
  (get-in (.getData component) (parse-key key)))

(defn detach-data
  "Get and remove any attached data at key"
  [component key]
  (let [ks (parse-key key)
        ret (get-data component ks)]
    (.setData
     component
     (cond
       (= 1 (count ks)) (dissoc (.getData component) (first ks))
       true (let [front (take (dec (count ks)) ks)
                  last (last ks)]
              (update-in (.getData component) front #(dissoc %1 last)))
       ))
    ret))

(defn humanize
  "Turn a keyword or symbol string into a humanized for. The text is split at hyphens (-) and each segment is capitalized"
  [kw-or-string]
  (str/join " " (map capitalize (str/split (name kw-or-string) #"-"))))

(defn extract-keys
  "Extract the keys and values from m whose keys appear in rmkeys. Return the extracted map and the remaining map"
  [m rmkeys]
  (reduce (fn [[l r] k]
            (if ((set (keys r)) k)
              [(assoc l k (get r k)) (dissoc r k)]
              [l r]))
          [{} m] rmkeys)
  )

(defn get-field-group [component]
  (and component (get-data component :field-group)))

(defn set-field-group [component fg]
  {:pre [(not (nil? component))]}
  (attach-data component :field-group fg))

(defn form-of
  "Return the formthe component is a member of. Defined as the first parent component with a field group."
  [component]
  (if component
    (if (get-field-group component)
     component
     (recur (.getParent component)))))

(defn bad-argument [& args]
  (throw (IllegalArgumentException. ^String (apply str args))))

(defn unsupported-op [& args]
  (throw (UnsupportedOperationException. ^String (apply str args))))