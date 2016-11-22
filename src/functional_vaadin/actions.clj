(ns functional-vaadin.actions
  "Useful public functions for creating and manipulatinig Actions"
  (:require [clojure.spec :as s])
  (:use [functional-vaadin.utils])
  (:import (com.vaadin.event Action$Listener ShortcutAction Action$Handler Action)))

(defn ->FunctionAction
  "Usage: (->Action caption icon? action-fn)

  Create an Action that executes action-fn when it is activated. This can be added to any Component that implements
  com.vaadin.Action.Notiifer, and also supplied as an Action to functional_vaadin.action.ActionHandler.

  The action-fn is called as (action-fn action sender target)"
  ([caption icon action-fn]
   (proxy [Action Action$Listener] [caption icon]
     (handleAction [sender target] (action-fn this sender target))))
  ([caption action-fn]
    (->FunctionAction caption nil action-fn))
  )

(defn ->ShortcutAction
  "Usage: (->ShortcutAction caption keycode action-fn modifiers?)
          (->ShortcutAction [caption resource] keycode action-fn modifiers?)


  Create a ShortcutAction that executes action-fn when fired. The action function is passed the Action (this),
  the sender and the target
  "
  ([ident keycode action-fn modifiers]
   (cond
     (instance? String ident)
       (proxy [ShortcutAction Action$Listener] [ident (int keycode) (int-array modifiers)]
         (handleAction [sender target] (action-fn this sender target)))
     (and (vector? ident) (= 2 (count ident)))
       (proxy [ShortcutAction Action$Listener] [(first ident) (second ident) (int keycode) (int-array modifiers)]
         (handleAction [sender target] (action-fn this sender target)))
     :else (bad-argument "Incorrect name: " ident ". Shortcut name must be a String or Vector of caption and icon")
     ))
  ([ident keycode a-fn] (->ShortcutAction ident keycode a-fn []))
  )

(deftype ActionHandler [select-fn handle-fn actions]
  Action$Handler
  (getActions [this target sender]
    (into-array (select-fn target sender actions)))
  (handleAction [this action sender target]
    (handle-fn action sender target)
    )
  )

(defn dispatch-listener [action sender target]
  (.handleAction action sender target))

(defn all-actions [target sender actions]
  actions)

;(defn ->ActionHandler
;  "Usage: (->ActionHandler select-fn handler-fn actions)
;          (->ActionHandler select-fn actions)
;          (->ActionHandler actions)
;
;  Create and ActionHandler from a set of actions that uses select-fn to select an Action for a sender and target, and
;  handler-fn to handle it.
;
;  Defaults are to select all actions, and to dispatch as an Action$Listener. This allows and ActionHandler to be created that
;  acts as a collection of Action$Listeners"
;  ([select-fn handler-fn actions]
;   (println select-fn handler-fn actions)
;    #functional_vaadin.actions.ActionHandler[select-fn handler-fn actions])
;  ([select-fn actions]
;    (->ActionHandler select-fn dispatch-listener actions))
;  ([actions]
;    (->ActionHandler all-actions dispatch-listener actions)))




