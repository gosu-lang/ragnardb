package ragnardb.plugin;

import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.IType;

public interface ISQLQueryType extends ISQLTypeBase {
  public ISQLTableType getTable(String name);
}
