package ragnardb.plugin;

import java.util.Set;

public interface ISQLSource {

  /**
   * The names of all the SQL types available from this source
   * @return a non null set of SQL type names
   */
  Set<String> getTypeNames();

}
