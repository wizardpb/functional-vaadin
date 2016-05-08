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

## Release

Currently just a snapshot release - v0.1.0-SNAPSHOT

## Usage


## License

Copyright © 2016 Prajna Inc, all rights reserved

Distributed under the Eclipse Public License either version 1.0 or any later version.
