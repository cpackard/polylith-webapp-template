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

See [sql/config.edn](resources/sql/config.edn) for full settings.

## Migrations

This component currently uses [migratus](https://github.com/yogthos/migratus) as the migration framework and [clj-migratus](https://github.com/paulbutcher/clj-migratus) as the CLI migration runner.

### Creating Migrations

`Migratus` allows for code-based migrations which integrate well with the Polylith architecture. An example of the workflow is given below; see the [migratus docs](https://github.com/yogthos/migratus#defining-a-code-based-migration) for more details.

To create a new migration, run the `sql/create-migration!` function with two arguments:

1. A dash-separated name for your migration, and
2. A string or quoted symbol that resolves to a `migrations` directory at the root level of any component.

```clojure
(require '[poly.web.sql.interface :as sql])
(sql/create-migration! "create-users-table" 'poly.web.user.migrations)
```

#### Generate the migration file

The above function creates two new files:

1. An EDN file like `20221231190748-create-users-table.edn`, a second-level timestamp prefixed to the migration name, and
2. A Clojure file with the name of your migration located in your migration namespace, like `components/user/src/poly/web/user/migrations/create_users_table.clj`.

The generated `.edn` file looks like this. You normally won't have to edit this, but you can view the Migratus docs for more info on their meaning and use.

```clojure
{:ns poly.web.user.migrations.create-users-table
 :up-fn migrate-up
 :down-fn migrate-down
 :transaction? true}
```

#### Creating the migration commands

Below is an example of the default generated migration file. The only required functions are `migrate-up` and `migrate-down` (or the corresponding names of `:up-fn` and `:down-fn`) which take a single `config` parameter. Inside these functions you can run anything you like to apply or rollback your migration.

```clojure
(ns poly.web.user.migrations.create-users-table
  "Migration file to create the `users `table."
  (:require
   [clojure.spec.alpha :as s]
   [poly.web.sql.interface :as sql]
   [poly.web.sql.interface.spec :as spec]
   [poly.web.sql.interface.helpers :refer [create-table drop-table with-columns]]))

(s/fdef migrate-up
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-up
  [config]
  (let [ds    (:db config)
        table (-> (create-table :test_users :if-not-exists)
                  (with-columns [[:id :uuid [:not nil] [:primary-key]]
                                 [:name [:varchar 255] [:constraint :users--name] :unique]]))]
    (sql/query table {} ds)))

(s/fdef migrate-down
  :args (s/cat :config ::spec/migratus-config))

(defn migrate-down
  [config]
  (let [ds (:db config)]
    (sql/query (drop-table :test_users) {} ds)))

(comment
  (do
    ;(require '[poly.web.sql.interface :as sql])
    (sql/migrate!)))
```

Note the `comment` form at the bottom of the file - with this you can send that expression to the REPL and run the migrations directly afterwards.
