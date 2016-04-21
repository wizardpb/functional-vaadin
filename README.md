# functional-vaadin

A functional (Clojure) interface to the Vaadin Web UI Framework.

Using Vaadin from Clojure is generally straightforward, but involves the same kind of repetion as it does from straight Java - endless .setXXX calls, intermediate vars to hold sub structure, and various minor inconveniences in setting parameters (e.g. the inability to define expansion ratios on contained objects themselves). It would also be nice to have a functional way of connecting data to the UI ...

This lib is designed to fix these issues. It offers (will offer, for now :-) a purely declarative UI definition, homoiconic in the same way that Clojure itself is, along with a data binding mechanism that is inspired from the server approach of Om-Next. (Thanks to David Nolen !)

## Usage


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
