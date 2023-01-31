# `rest-api`

Exposes a public [REST](https://en.wikipedia.org/wiki/Representational_state_transfer)ful API to the outside world for component functionality. In order to achieve this, it uses [Pedestal](http://pedestal.io/) as the web framework. 

## Organization

There are four namespaces under the src directory of bases/rest-api:

- api.clj
- handler.clj
- main.clj
- middleware.clj

### api.clj

The `api.clj` namespace contains route definitions. The REST API looks like this: TODO:

### handler.clj

The `handler.clj` namespace is the place where we define our handlers. Since rest-api is the only place where our project exposes its functionality, its handler needs to call functions in different components via their interfaces.

### main.clj

The `main.clj` namespace contains a main function to expose the REST API via a Jetty server. If you look at the project configuration at `projects/realworld-backend/deps.edn` you'll notice that there are two aliases named `:aot` and `:uberjar`. With the help of those two aliases and `main.clj`, we can create an uberjar which is a single jar file that can be run directly on any machine that has Java runtime. Once the jar file is run, the main function defined under `main.clj` will be triggered and it will start the server.

### middleware.clj

The `middleware.clj` namespace contains TODO:
