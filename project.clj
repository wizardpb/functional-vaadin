(defproject com.prajnainc/functional-vaadin "0.1.0-SNAPSHOT"
  :description "A functional interface to Vaadin"
  ;:url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.vaadin/vaadin-server "7.6.5"]
                 [com.vaadin/vaadin-client-compiled "7.6.5"]
                 [com.vaadin/vaadin-themes "7.6.5"]]
  :profiles {:dev {:source-paths ["src" "dev"]
                   ;:dependencies []
                   }}
  )
