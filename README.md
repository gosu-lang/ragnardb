# ragnardb

RagnarDB is an experimental O/R framework for the Gosu programming language.

## Environment setup

### Prerequisites

* IJ 14 CE
* JDK 8
* git clone git@github.com:gosu-lang/ragnardb.git
* git clone git@github.com:gosu-lang/ragnardb-test.git

### IJ setup

Within this (ragnardb) project, take the following steps:

1. Settings -> Plugins -> Install Plugin from disk..., then look for and install "Intellij plugin development with Maven". Restart IJ if necessary.
2. Maven Projects -> Profiles -> enable "ij"
  * The project module "gosu-custom-types-plugin" icon should now look like a... plug
3. Create a plugin SDK, if not existing already.
  1. File -> Project Structure
  2. Platform Settings -> SDKs
  3. Add a new SDK pointing to the root of your IJ installation.  The SDK would likely be named "IDEA IC-141.1532.4"
    * It is probably also a good idea to make a matching IDEA_HOME environment variable
  4. Go to Project Settings -> Modules, select "gosu-custom-types-plugin".  Set its SDK to that of the previous step.
4. Add a run configuration of type plugin.  The name is irrelevant but make sure its classpath is "gosu-custom-types-plugin".
5. Run the plugin configuration, which will launch another "sandbox" IJ instance.

### Gosu sandbox setup

1. Get the ij-gosu plugin JAR from http://build/job/ij-gosu/ (Guidewire internal only)
2. Choose Configure -> Plugins -> Install Plugin from disk..., then select the JAR you downloaded
3. Shutdown the sandboxed IJ and restart from the run configuration again.
4. Open the root pom of the ragnardb-test project
5. Ctrl-N to find HelloWorldTest - you should see pretty Gosu syntax highlighting.  Right-click and run the test class.
