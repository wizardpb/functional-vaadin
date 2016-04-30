(ns functional-vaadin.data-binding.item
  (:import
    (com.vaadin.data.util ObjectProperty PropertysetItem)
    [java.util Collection Map Set]
    [com.vaadin.data Item]))

(defn- initializeItem
  "Initialize a given Item using updateFn to set a Property on the item from some element of kvdata.
  Kvdata is an iterable sequece of kv pairs (MapEnties, vectors, etc) and is iterated with reduce-kv. updateFn
  id called as (updateFn item pid val) where pid and value are the k-v values from kvdata"
  [item updateFn kvdata]
  (reduce
    (fn [item [pid val]] (updateFn item pid val) item)
    item
    kvdata)
  )

(defn ->Item
  "Create an Item from some kind of sequence data. For Maps, keys and values form Property ids and values.
  Sequences and vectors use their natural (integer) keys, Sets have keys sythezized by iterating in
  (random?) order"
  [data]
  (initializeItem
    (PropertysetItem.)
    (fn [item pid val]
      (.addItemProperty item pid (ObjectProperty. val)))
    (condp instance? data
      Collection (map-indexed #(vector %1 %2) data)
      Map data
      true (throw (IllegalArgumentException. "Cannot bind an Item to " data)))))

