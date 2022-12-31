<img src="logo.png" width="30%" alt="Polylith" id="logo">

# poly-web **(status: alpha)**

This repo aims to be a batteries-included template for quickly bootstrapping Clojure web projects following the Polylith architecture.

The end format will be similar to the [Clojure Polylith Realworld Example App](https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app) but with only the "core" components needed for most web-app functionality: database operations, auth middleware, etc. Check them out for great resource on a fully implemented Polylith codebase. 

`poly-web` is currently in active development (hence the alpha status). Suggestions and/or pull requests for functionality is welcome. 

To load all dependencies in the REPL, run the command `cider-jack-in-clj` from the [user.clj](development/src/user.clj) file.

## Polylith

The Polylith documentation can be found here:

- The [high-level documentation](https://polylith.gitbook.io/polylith)
- The [Polylith Tool documentation](https://polylith.gitbook.io/polylith/poly)
- The [RealWorld example app documentation](https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app)

You can also get in touch with the Polylith Team on [Slack](https://clojurians.slack.com/archives/C013B7MQHJQ).

## Specs

- Specs for functions are defined directly above the implementation (i.e. `core.clj` or its sub-namespaces).
- Specs defining generic ideas for a component live in `spec.clj`
- Specs defining generic ideas shared *across* components are defined in the `spec` component.
	- Other components import this via `(require '[poly.web.spec.interface :as spec])`
- If a component's spec is required by another component, that spec is defined like `components/sql/src/poly.web/sql/interface/spec.clj`
	- Other components import this via `(require '[poly.web.sql.interface.spec :as sql-spec])`
