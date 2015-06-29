# Design Ideas/Concepts

RagnarDB is a SQL ~~Typeloader~~ compiler plugin for Gosu: it will inspect SQL resources on disk and produce types corresponding to these resources,
and provide basic CRUD operations against a database based on this information.

## Components

RagnarDB will consist of four different components:

* A SQL Parser
* A Gosu ~~Type Loader~~ compiler plugin that uses the results of the SQL Parser to produce types
* A runtime that the ~~Type Loader~~ compiler plugin uses for producing objects to meet the runtime needs of the above types
* A runtime API that will expose whatever is necessary to RagnarDB users

### SQL Parser

The SQL Parser will consist of three different aspects:

* A Lexer
* The DDL Parser
* The SQL Parser

#### The DDL Parser

The DDL Parser will parse DDL files ending in *.ddl according to (TODO: luca - enter a web link)

    CREATE TABLE Persons
    (
      PersonID int,
      LastName varchar(255),
      FirstName varchar(255),
      Address varchar(255),
      City varchar(255)
    );

#### The SQL Parser

The SQL Parser will parse SQL files ending in *.sql according to (TODO: luca - enter a web link)

    SELECT * 
    FROM Persons
    WHERE FirstName = 'Brian';

The SQL parser will also need to support the following syntax to allow the passing of values into a query:

    SELECT * 
    FROM Persons
    WHERE FirstName = @name:java.lang.String;

This inline parameter syntax indicates that this query takes an argument, `name` of type `java.lang.String`.  The type 
implications of this are discussed below.

A variable can be used more than once and only needs to include the type the first time:

    SELECT * 
    FROM Persons
    WHERE FirstName = @name:java.lang.String OR
          LastName = @name;

### The SQL ~~Typeloader~~ compiler plugin

The plugin will take the results of the parser above and produce types.  For the DDL files, this is fairly
straight-forward:  given a ddl file found at `/model/application.ddl` with the `Persons` table defined above,
the following type will be created `model.application.Person`, where the package for the type is the path 
to the DDL file, plus the file name, and then the singularized version of the table name.

The properties of `model.application.Person` would be the obvious properties implied by the column definitions.

For SQL Files, things are a bit more complex.  Consider two different aspects of the SQL files:

* The return type of the query
* The argument types of the query

Given a SQL file found at `/model/PersonsByName.sql`, the following type will be created: `model.PersonsByName`.  This 
type will have a single method, `execute`, which will execute the given SQL.

The arguments to `execute` are straight forward: they are determined by the inline parameter syntax outlined above.

The return type of `execute` is less straight forward.  For version 1 of the project, lets assume that all queries
will be defined as `SELECT * FROM X`.  In that case, the return type will be a ResultSet/Iterable of `X`, where `X`
is a type resolved in the DDL file found in the same (or parent) directory.

### The Runtime

The runtime behavior and API will be derived, as much as is possible, from the Active Record runtime, which has proven
itself flexible and pleasent enough for many developers to work with.

## Design Goals

* The core idea is: "Let people use SQL against a DB rather than a custom query language or builder syntax".
* "Sloppy" parsing will be used, where we pass over/pass through SQL we don't understand to the underlying JDBC layer
* The project will avoid binary thinking, such as "We will parse everything OR we will parse nothing."
* The project will focus on simplicity and 80/20 thinking

## Open Questions

* Do we need to support an ID concept?  Can we discard that idea now?
* Transactions?
* Logging/Debugging SQL?
* Tracing SQL?