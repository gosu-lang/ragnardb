package ragnardb.plugin;

import gw.fs.IResource;

import java.util.Set;

public interface ISQLSource extends IResource {

  /**
   * The names of all the SQL types available from this source
   * @return a non null set of SQL type names
   */
  Set<String> getTypeNames();

}
