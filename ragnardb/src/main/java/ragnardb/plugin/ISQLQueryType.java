package ragnardb.plugin;

import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.IType;
import ragnardb.parser.ast.SelectStatement;
import ragnardb.parser.ast.Statement;

import java.util.ArrayList;

public interface ISQLQueryType extends ISQLTypeBase {
  public ISQLTableType getTable(String name);

  public IType getColumn(String name, String tableName);

  public SQLPlugin getPlugin();

  public ISQLQueryResultType getResults(Statement statement, ISQLQueryType type);

  public ISQLQueryResultType getResults(ArrayList<SQLColumnPropertyInfo> columns, Statement statement);

  public SQLColumnPropertyInfo getColumnProperty(String colName, String tableName);

  public ISQLQueryResultType getResultType();

  public void setResultType(ISQLQueryResultType type);
}
