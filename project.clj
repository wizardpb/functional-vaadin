(defproject com.prajnainc/functional-vaadin "0.1.0-BETA"
  :description "A functional interface to Vaadin"
  :url "https://github.com/wizardpb/functional-vaadin"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-codox "0.9.5"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.reactivex/rxclojure "1.0.0"]
                 [com.vaadin/vaadin-server "7.6.5"]
                 [com.vaadin/vaadin-client-compiled "7.6.5"]
                 [com.vaadin/vaadin-themes "7.6.5"]]
  :main functional-vaadin.examples.run
  :codox {:namespaces [functional-vaadin.core
                       functional-vaadin.conversion
                       functional-vaadin.rx.observers
                       functional-vaadin.rx.operators
                       functional-vaadin.examples.Sampler]}
  :profiles {:provided {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]}
             :dev      {:aot [functional-vaadin.ui.TestUI
                              functional-vaadin.examples.Sampler]
                        :source-paths ["src" "dev"]
                        :dependencies [[org.apache.directory.studio/org.apache.commons.io "2.4"]
                                       [org.clojure/tools.nrepl "0.2.11"]
                                       [org.eclipse.jetty/jetty-server "9.3.8.v20160314"]
                                       [org.eclipse.jetty/jetty-servlet "9.3.8.v20160314"]
                                       ]
                        }
             :jar {:aot [functional-vaadin.ui.TestUI
                         functional-vaadin.examples.Sampler]}}
  )
