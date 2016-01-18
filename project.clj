(defproject functional-vaadin "0.1.0-SNAPSHOT"
  :description "A functional interface to the Vaadin Web Framework"
  :url ""

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.vaadin/vaadin-server "7.6.1"]
                 [com.vaadin/vaadin-client-compiled "7.6.1"]
                 [com.vaadin/vaadin-themes "7.6.1"]
                 ;;[javax.servlet/servlet-api "2.5"]
                 ]
  :aot [examples.simple.main-ui]
  :plugins [[lein-servlet "0.4.1"]]

  :servlet {:deps    [[lein-servlet/adapter-jetty9 "0.4.1" :exclusions [org.glassfish/javax.el]]]
            :config  {:engine :jetty
                      :host   "localhost"
                      :port   3000}
            :webapps {"/"
                      {:web-xml "src/main/webapp/WEB-INF/web.xml"
                       :public  "resources"}}})
