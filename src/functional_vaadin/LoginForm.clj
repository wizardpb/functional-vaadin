(ns functional-vaadin.LoginForm
  (:gen-class :extends com.vaadin.ui.LoginForm
              :main false
              :state state
              :init init-my-state
              :methods [
                        [setLoginButtonFunc [clojure.lang.IFn] void]
                        [setUsernameFieldFunc [clojure.lang.IFn] void]
                        [setPasswordFieldFunc [clojure.lang.IFn] void]
                        ]
              :exposes-methods {createLoginButton superCreateLoginButton
                                createUsernameField superCreateUsernameField
                                createPasswordField superCreatePasswordField}))

(defn -init-my-state []
  [[] (atom {})])

(defn- setState [state key value]
  (swap! state #(assoc % key value)))

(defn- get-state [state key]
  (get @state key))

(defn -setLoginButtonFunc [this func]
  (setState (.state this) :loginButtonFunc func))

(defn -setUsernameFieldFunc [this func]
  (setState (.state this) :usernameFieldFunc func))

(defn -setPasswordFieldFunc [this func]
  (setState (.state this) :passwordFieldFunc func))

(defn -createLoginButton [this]
  (if-let [func (get-state (.state this) :loginButtonFunc)]
    (func)
    (.superCreateLoginButton this)))

(defn -createUsernameField [this]
  (if-let [func (get-state (.state this) :usernameFieldFunc)]
    (func)
    (.superCreateUsernameField this)))

(defn -createPasswordField [this]
  (if-let [func (get-state (.state this) :passwordFieldFunc)]
    (func)
    (.superCreatePasswordField this)))

