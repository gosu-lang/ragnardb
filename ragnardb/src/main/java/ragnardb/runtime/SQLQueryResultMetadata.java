package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import ragnardb.plugin.ISQLQueryResultType;
import ragnardb.plugin.ISQLTableType;
import ragnardb.plugin.SQLColumnPropertyInfo;


/**
 * Created by klu on 7/10/2015.
 */
public class SQLQueryResultMetadata implements ITypeToSQLMetadata{
  private ISQLTableType table;

  public SQLQueryResultMetadata(ISQLQueryResultType resultType){
    table = resultType.getTable();
  }

  public SQLQueryResultMetadata(ISQLTableType tab){
    table = tab;
  }

  @Override
  public String getTableForType(IType type) {
    return table.getTable().getTableName();
  }

  @Override
  public String getColumnForProperty(IPropertyInfo pi) {
    SQLColumnPropertyInfo asSQL = (SQLColumnPropertyInfo) pi;
    return asSQL.getColumnName();
  }
}
