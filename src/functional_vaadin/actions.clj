(ns functional-vaadin.actions
  "Useful public functions for creating and manipulatinig Actions, It provides a new type ActionHandler, implementing Action$Handler. This
  allows for the customization of action selection and handling by providing two functions, a select function and a handler function, along
  with a list of Actions to choose from."
  (:require [clojure.spec.alpha :as s])
  (:use [functional-vaadin.utils])
  (:import (com.vaadin.event Action$Listener ShortcutAction Action$Handler Action)))

(defn ->FunctionAction
  "Usage: (->FunctionAction caption icon? action-fn)

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


  Create a ShortcutAction that executes action-fn when fired. The action function is called as
  (action-fn action sender target)"
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

(defn dispatch-listener
  "A convenience handler function for ActionHandler that displatches an Action as an Action$Listener. Actions held in this
  ActionHandler must therefore be Action$Listeners (->FunctionalAction) creates such actions"
  [action sender target]
  (.handleAction action sender target))

(defn all-actions
  "A convenience action selection function for an ActionHandler. Simply returns all available actions"
  [target sender actions]
  actions)




