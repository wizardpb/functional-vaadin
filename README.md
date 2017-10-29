# functional-vaadin

A functional (Clojure) interface to the Vaadin Web UI Framework.

Using Vaadin from Clojure is generally straightforward, but involves the same kind of repetion as it does from straight
Java - endless .setXXX calls, intermediate vars to hold sub structure, and various minor inconveniences in setting
parameters (e.g. the inability to define expansion ratios on contained objects themselves). It would also be nice to
have a functional way of connecting data to the UI.

This lib is designed to fix these issues. It offers

- a purely declarative UI DSL, homoiconic in the same way that Clojure itself is
- a component naming mechanism that removes the need for variables to refer to components, available at both
construction and run time.
- a simpler event handing mechanism, along with integration with RxClosure
- conversion functions to interface Clojures immutable data structures with Vaadin data binding objects

## Latest Release

![](https://clojars.org/com.prajnainc/functional-vaadin/latest-version.svg)

## Install
Add the following to you project.clj file:

        [com.prajaninc/functional-vaadin "0.1.1"]

You will also need to add dependencies for your choosen Vaadin libraries e.g.

        [com.vaadin/vaadin-server "7.6.5"]
        [com.vaadin/vaadin-client-compiled "7.6.5"]
        [com.vaadin/vaadin-themes "7.6.5"]


## Usage
Require the namespaces you need:

Primary namespace, containing all the builder functions:

    (require [functional-vaadin.core :refer :all])

RxClojure integrations are in:

    (require [functional-vaadin.rx.observers :as obs]
             [functional-vaadin.rx.operators :as ops])

## Documentation
Guides and documentation are available on the [project wiki](https://github.com/wizardpb/functional-vaadin/wiki)

API specifications are [here](http://prajnainc.com/functional-vaadin/doc/)

## Demos and Examples

There is a simple Sampler application included in the repo. A more complete one is an implementation
of ToDoMVC [here](https://github.com/wizardpb/todo)

## License

Copyright © 2016 Prajna Inc, all rights reserved

Distributed under the Eclipse Public License either version 1.0 or any later version.
