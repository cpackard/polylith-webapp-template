<img src="logo.png" width="30%" alt="Polylith" id="logo">

# poly-web **(status: alpha)**

This repo aims to be a batteries-included template for quickly bootstrapping Clojure web projects following the [Polylith architecture](https://polylith.gitbook.io/polylith/).

The end format will be similar to the [Clojure Polylith Realworld Example App](https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app) but with only the "core" components needed for most web-app functionality: database operations, auth middleware, etc. Check them out for great resource on a fully implemented Polylith codebase. 

`poly-web` is currently in active development (hence the alpha status). Suggestions and/or pull requests for functionality is welcome. 

To load all dependencies in the REPL, run the command `cider-jack-in-clj` from the [user.clj](development/src/user.clj) file.

## Building and Running

To build the project: `clojure -T:build uberjar :project backend`

To run the compiled JAR file: `java -jar projects/backend/target/backend.jar`

## Polylith

The Polylith documentation can be found here:

- The [high-level documentation](https://polylith.gitbook.io/polylith)
- The [Polylith Tool documentation](https://polylith.gitbook.io/polylith/poly)
- The [RealWorld example app documentation](https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app)

You can also get in touch with the Polylith Team on [Slack](https://clojurians.slack.com/archives/C013B7MQHJQ).

## Structure and Organization

The [Polylith architecture docs](https://polylith.gitbook.io/polylith/architecture/2.1.-workspace) provide details about the general organization patterns used here.

Some repo-specific conventions:

- All components which `require` another component's interface alias it by the component name: `(require '[poly.web.user.interface :as user])`
  - The `logging` component is frequently aliased as `log` for brevity.
- Component's public specs are organized in the corresponding `interface.spec` namespace and aliased with the "-s" prefix: `(require '[poly.web.user.interface.spec :as user-s])`

## Specs

This repo uses the [spec library](https://clojure.org/guides/spec) for data validation, test data generation, and instrumentation during development.

General guidelines for organization (from an [ask.clojure.org question](https://ask.clojure.org/index.php/9036/sharing-specs-between-modules)):

- *Type specs* (component agnostic, i.e. `non-blank-string?`) are implemented in the `spec` component to be shared across others.
- *Value specs* (non-divisible data meaningful to your app) are kept in the corresponding `interface.spec` namespace.
  - As an example, `(s/def ::first-name string?)` would be a value spec in `poly.web.user.interface.spec` because it distinguishes a user's first name from their last name. However, a full User spec like `(s/def ::user (s/keys ...))` would *not* be a value spec, because you often work with subsets of user fields, like only their email/password during login. This example would be a *structural spec* (see below).
  - Since these live in the `interface` namespace these specs are also public to other components.
- *Structural specs* (temp arrangement of *value specs* into a collection or structure) may live anywhere else.
  - Typically things like `s/keys`, `s/map-of`, `s/tuple`, `s/coll-of`, etc. are structural specs.
  - Recommendation is to keep these definitions as close to their usages as possible (like `s/fdef` directly above `defn`).

# License

Distributed under the [MIT License](https://opensource.org/licenses/MIT), the same as the [Clojure Polylith RealWorld Example App](https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app) project.
