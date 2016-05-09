(ns functional-vaadin.examples.run
  "A main function namespace to run examples in a jetty container"
  (:import (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.servlet DefaultServlet ServletContextHandler)
           [com.vaadin.server VaadinServlet]
           )  )

(defn run-jetty [ui-name bg?]
  (println "Running " ui-name (if bg? " in background" ""))
  (let [server (Server. 8080)
        ^ServletContextHandler context (ServletContextHandler. ServletContextHandler/SESSIONS)]
    (.setContextPath context "/")
    (.setInitParameter context "UI" ui-name)
    (.setResourceBase context "dev-resources/public")

    (.setHandler server context)
    (.addServlet context VaadinServlet "/*")

    (.start server)
    (if bg?
      server
      (.join server)
      )))

(defn -main
  ([join? ui-name] (run-jetty ui-name (= (first join?) "-b")))
  ([ui-name] (run-jetty ui-name false))
  )
