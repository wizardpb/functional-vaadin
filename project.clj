(defproject com.prajnainc/functional-vaadin "0.3.0-snapshot"
  :description "A functional interface to Vaadin"
  :url "https://github.com/wizardpb/functional-vaadin"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-codox "0.9.5"]
            [lein-pprint "1.1.1"]]
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [io.reactivex/rxclojure "1.0.0"]]
  :codox {:namespaces [functional-vaadin.core
                       functional-vaadin.conversion
                       functional-vaadin.validation
                       functional-vaadin.rx.observers
                       functional-vaadin.rx.operators]}
  :profiles {:provided {:dependencies [[com.vaadin/vaadin-server "7.7.3"]
                                       [com.vaadin/vaadin-client-compiled "7.7.3"]
                                       [com.vaadin/vaadin-themes "7.7.3"]
                                       [org.eclipse.jetty/jetty-server "9.3.8.v20160314"]
                                       [org.eclipse.jetty/jetty-servlet "9.3.8.v20160314"]
                                       [javax.servlet/javax.servlet-api "3.1.0"]]}
             :dev      {:aot          [functional-vaadin.ui.LoginForm
                                       functional-vaadin.ui.TestUI
                                       functional-vaadin.examples.Sampler
                                       functional-vaadin.examples.run]
                        :main         functional-vaadin.examples.run
                        :source-paths ["src" "dev"]
                        :dependencies [[org.apache.directory.studio/org.apache.commons.io "2.4"]
                                       [org.clojure/tools.nrepl "0.2.11"]
                                       [org.clojure/tools.namespace "0.2.11"]
                                       ]
                        }
             :jar      {:aot          [functional-vaadin.ui.LoginForm]
                        ;:dependencies [[com.vaadin/vaadin-server "7.7.3"]
                        ;               [com.vaadin/vaadin-client-compiled "7.7.3"]
                        ;               [com.vaadin/vaadin-themes "7.7.3"]
                        ;               [org.eclipse.jetty/jetty-server "9.3.8.v20160314"]
                        ;               [org.eclipse.jetty/jetty-servlet "9.3.8.v20160314"]
                        ;               [javax.servlet/javax.servlet-api "3.1.0"]]
                        }
             :uberjar  {:aot          [functional-vaadin.ui.LoginForm functional-vaadin.examples.Sampler functional-vaadin.examples.run]
                        :main         functional-vaadin.examples.run
                        ;:dependencies [[com.vaadin/vaadin-server "7.7.3"]
                        ;               [com.vaadin/vaadin-client-compiled "7.7.3"]
                        ;               [com.vaadin/vaadin-themes "7.7.3"]
                        ;               [org.eclipse.jetty/jetty-server "9.3.8.v20160314"]
                        ;               [org.eclipse.jetty/jetty-servlet "9.3.8.v20160314"]
                        ;               [javax.servlet/javax.servlet-api "3.1.0"]]
                        }
             }
  )
