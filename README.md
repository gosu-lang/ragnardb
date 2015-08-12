# RagnarDB

RagnarDB is an experimental O/R framework for the Gosu programming language.

## Parsing


## DDL Files

*TODO: Describe DDL semantics*

## SQL Query Files

.sql files are also handled in this plugin. Upon creation of an SQL file and import into a gosu class or program file,
the name of the SQL file can be directly addressed as a variable. This query type has one relevant property and one
relevant method.

sqlSource is a property which allows you to directly access, as a string, the text of the file. This property is not 
recommended for use against a database, however, it may have uses in Gosu.

To run this query against a database, use the method execute. execute does two things: first, execute will parameterize
(see **Query Parameters** for details) parts of the file. execute will then create a preparedStatement and run it against
the database. What execute returns depends on the contents of the file; for all queries which are not selection queries,
execute returns an integer of the number of columns affected. For selection queries of entire tables, execute returns the
table type (see **DDL Files** for details). For single columns, execute returns a type appropriate for the type of the
column. For all other queries, execute returns a created query result type with all the relevant columns accessible.

*Currently known issues:*

execute does not handle multiple statements in an sql file; this is an intentional design choice to simply naming, 
parameterization, return type handling, on the basis that multiple queries can simply be put into multiple files.



### Query Parameters

## Runtime API

*TODO: Describe DDL Types runtime API (e.g. save())*

### Query Builder API

### Gosu Extension API

### Validation API

### Property Listeners

### Life Cycle Callback API

## Developing RagnarDB

### Environment Setup

#### Prerequisites

* IJ 14 CE
* JDK 8
* `git clone git@github.com:gosu-lang/ragnardb.git`
* `git clone git@github.com:gosu-lang/ragnardb-test.git`
* The following environment variables defined:
  * JAVA_HOME
  * IDEA_HOME
  * IDEA_JDK

#### IJ setup

Within this (ragnardb) project, take the following steps:

1. Settings -> Plugins -> Browse Repositories..., then look for and install "Intellij plugin development with Maven". Restart IJ if necessary.
2. Maven Projects -> Profiles -> enable "ij"
  * The project module "gosu-custom-types-plugin" icon should now look like a... plug
3. Create a plugin SDK, if not existing already.
  1. File -> Project Structure
  2. Platform Settings -> SDKs
  3. Add a new SDK pointing to the root of your IJ installation.  The SDK would likely be named "IDEA IC-141.1532.4"
    * It is probably also a good idea to make a matching IDEA_HOME environment variable
  4. Go to Project Settings -> Modules, select "gosu-custom-types-plugin".  Set its SDK to that of the previous step.
4. Add a run configuration of type plugin.  Call it "Gosu Sandbox" although the name is irrelevant. Make sure its classpath is "gosu-custom-types-plugin".
5. Run the plugin configuration, which will launch another "sandbox" IJ instance.

#### Gosu sandbox setup

1. Get the ij-gosu plugin JAR from http://build/job/ij-gosu/ (Guidewire internal only)
2. Choose Configure -> Plugins -> Install Plugin from disk..., then select the JAR you downloaded
3. Shutdown the sandboxed IJ and restart from the run configuration again.
4. Open the root pom of the ragnardb-test project
5. Ctrl-N to find HelloWorldTest - you should see pretty Gosu syntax highlighting.  Right-click and run the test class.
6. Upon startup of the sandbox, you may see a " Gosu plugin could not start: Project SDK not defined." error.
  * To resolve this, go to File -> Project Structure, Platform Settings -> SDKs and add a 1.8 SDK if it does not exist already.
  * Go to Project Settings -> Project and set the JDK to 1.8, also make sure the language level is set to 8 or the sdk default.
