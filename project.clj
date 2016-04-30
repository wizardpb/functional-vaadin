(defproject com.prajnainc/functional-vaadin "0.1.0-SNAPSHOT"
  :description "A functional interface to Vaadin"
  ;:url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.vaadin/vaadin-server "7.6.5"]
                 [com.vaadin/vaadin-client-compiled "7.6.5"]
                 [com.vaadin/vaadin-themes "7.6.5"]]
  :aot [functional-vaadin.TestUI]
  :profiles {:provided {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]}
             :dev      {:source-paths ["src" "dev"]
                        :dependencies [[org.apache.directory.studio/org.apache.commons.io "2.4"]
                                       [org.clojure/tools.nrepl "0.2.11"]
                                       [org.eclipse.jetty/jetty-server "9.3.8.v20160314"]
                                       [org.eclipse.jetty/jetty-servlet "9.3.8.v20160314"]
                                       ]
                        }}
  )
