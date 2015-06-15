package ragnardb.plugin;

import gw.fs.IFile;
import gw.lang.reflect.IType;

import java.util.Set;

public interface ISQLSource {

  /**
   * The names of all the SQL types available from this source
   * @return a non null set of SQL type names
   */
  Set<String> getTypeNames();


  /**
   * Get the type from this source by name
   * @param name
   * @return IType for the given type name
   */
  IType getTypeByName(String name); //TODO strongly type?


  /**
   * Possibly not useful?
   * @param file
   * @return set of all SQL types from the given file
   */
  static Set<IType> getTypesFromFile(IFile file) {

    return null;
  }

//  Set<IType> getTypes();

  /**
   * Returns the file for the given name.
   */
  IFile getFile(String name);



}
