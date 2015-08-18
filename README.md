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

-execute does not handle multiple statements in an sql file; this is an intentional design choice to simply naming, 
parameterization, return type handling, on the basis that multiple queries can simply be put into multiple files.

-subqueries automatically trigger a query result type with all properties as objects

-query result types do not fully extend sql record at the moment, creating some problems with field access

### Query Parameters

With normal SQL files, using execute simply runs the query. We have extended the SQL grammar to support parameterization,
in the form of replacing normal expressions with @ to indicate the use of a parameter. @variablename alerts the plugin to
a new parameter, variablename. When any particular variable name is used the first time, the full variable type must also
be stated (a parse error will result if this is not the case). @age:java.lang.Integer alerts the plugin to a new parameter
called age of type int. When execute is called, it expects as many arguments as there are formal parameters given. In the
execution, all parameters are replaced with the arguments. Note that if you use a parameter name multiple times, it only
appears as one formal parameter and all instances of it will be replaced by the same argument value. 

## Runtime API

*TODO: Describe DDL Types runtime API (e.g. save())*

### Query Builder API

### Gosu Extension API

### Validation API

See **Gosu Extension API** for an overview of how to extend table types. The method configure is an empty method in all
table types which should be overridden in the extension class to enable validation. A model configurator is automatically
created; validators can be added to this configurator. A validator has the method validate which accepts a property
reference. The method returns null, but throws an exception if validation fails. Adding validators modifies the save
method (see **Runtime API** for details).

*Default Validators*

The following methods of the model configurator create default validators. All validators throw a validation exception, 
which is a subclass of runtime exception on validation failure.

-requiredFields: Expects a list of property references. Equivalent to the SQL declaration NOT NULL on a column; demands
that the the property is not null. Fails if the property reference has not been instantiated or is null.

-lengthBetween: Expects a property reference, a minimum, and a maximum length. To specify no maximum length, enter -1.
Fails if the property reference has not been instantiated or the property's string representation is outside the 
inclusive range of [minimum, maximum].

-validateFormat: Expects a property reference and a regex string. Fails if the property reference has not been
instantiated or its string representation does not match the regex. Can be used with non strings, although results may
not be predictable.

-unique: Expects a property reference. Equivalent to the SQL declaration UNIQUE on a column; demands that this value does
not occur elsewhere in this table. Fails if the property reference has not been instantiated or is not unique in this
table.

-hasContent: Expects a property reference. Equivalent to calling lengthBetween with the parameters (property reference,
1, -1). Fails if the property reference has not been instantiated or its string representation is empty.

-isInSet: Expects a property reference and a list or set of values. Fails if the property reference has not been
instantiated or its value is not within the group of accepted values. Note that this functions ultimately by calling 
equals in comparison.

*Custom Validators*

The model configurator also has an addValidator method. This expects a property reference and a validator, which can be
an inline function. The inline function should have a test and throw an exception if this test fails.

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
