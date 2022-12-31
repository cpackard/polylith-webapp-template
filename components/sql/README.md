# sql component

Component containing functions related to the SQL database.

## Libraries

This component currently uses:
- [honeysql](https://github.com/seancorfield/honeysql) for writing SQL as Clojure data structures.
- [HickariCP](https://github.com/brettwooldridge/HikariCP) for JDBC connection pools.
- [next.jdbc](https://github.com/seancorfield/next-jdbc) as a Clojure wrapper for JDBC-based access to databases.
- [migratus](https://github.com/yogthos/migratus) as the DB migration framework.
- [integrant](https://github.com/weavejester/integrant) as the micro-framework for managing software components which have runtime state.


## Database Setup

This project assumes you have a [PostgreSQL](https://www.postgresql.org/) instance running with the following settings:
- host: `localhost`
- port: `5432`
- database (name): `postgres`
- user: `pguser`
- password `pgpass`

See [sql/config.edn](components/sql/resources/sql/config.edn) for full settings.

## Migrations

This component currently uses [migratus](https://github.com/yogthos/migratus) as the migration framework and [clj-migratus](https://github.com/paulbutcher/clj-migratus) as the CLI migration runner.

### Invoking Commands

Migration commands can be invoked either from a connected REPL (*recommended* - see [migratus.clj](components/sql/src/poly.web/sql/migratus.clj:17:0) for full examples of each command) or from the CLI at the root of the repo:

```shell
clj -M:migrate <COMMAND>
```

See the [migratus usage docs](https://github.com/yogthos/migratus#usage) for full documentation.

### Creating Migrations

`Migratus` allows for code-based migrations which integrate well with the Polylith architecture. An example of the workflow is given below; see the [migratus docs](https://github.com/yogthos/migratus#defining-a-code-based-migration) for more details.

#### Generate the migration file

This will create an empty `.edn` file in the folder specified by the `:migrations-dir` key in the [migratus.clj](components/sql/src/poly.web/sql/migratus.clj:9:3) file. Here we give an example name "create-users-table" for the migration, so the full name of the generated file will look like '20111202091200-add-users.edn'.

```clojure
(require '[migratus.core :as migratus])
(require '[poly.web.sql.migratus :as sqlm])
(migratus/create sqlm/config "create-users-table" :edn)
```

#### Populate the migration file

Open the generated `.edn` file from the previous step and add the following:

```clojure
{:ns poly.web.user.migrations.create-users-table
 :up-fn migrate-up
 :down-fn migrate-down
 :transaction? true}
```

#### Creating the migration commands

Next, define the migration commands in the same namespace as specified in the `:ns` key of the `.edn` file from the previous step. These files should live in the appropriate component's `migrations/` folder.

In the below example, the actual file would be `components/user/src/poly.web/user/migrations/create_users_table.clj`:

```clojure
(ns poly.web.user.migrations.create-users-table
  "Migration file to create the `users` table."
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.spec :as spec]
   [honey.sql.helpers :refer [create-table drop-table with-columns]]))

(s/fdef migrate-up
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-up
  [config]
  (let [ds    (:db config)
        table (-> (create-table :users :if-not-exists)
                  (with-columns [[:id :uuid [:not nil] [:primary-key]]
                                 [:name [:varchar 255] [:constraint :users--name] :unique]]))]
    (sql/query ds table)))

(s/fdef migrate-down
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-down
  [config]
  (let [ds (:db config)]
    (sql/query ds (drop-table :users))))

(comment
  (do
    (require '[poly.web.sql.migratus :as sqlm])
    (require '[migratus.core :as migratus])
    (migratus/migrate sqlm/config)))
```

Note the `comment` form at the bottom of the file - with this you can send that expression to the REPL and run the migrations directly afterwards.
