package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import ragnardb.plugin.SQLColumnPropertyInfo;

import java.util.ArrayList;

/**
 * Created by klu on 7/1/2015.
 */
public class SQLMetadata  implements ITypeToSQLMetadata{

  public SQLMetadata() {
  }

  public String getTableForType(IType type){
    return type.getRelativeName();
  }

  public String getColumnForProperty(IPropertyInfo pi){
    SQLColumnPropertyInfo asSQL = (SQLColumnPropertyInfo) pi;
    return asSQL.getColumnName();

  }
}
