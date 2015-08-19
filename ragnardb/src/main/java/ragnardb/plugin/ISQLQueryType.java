package ragnardb.plugin;

import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.IType;
import ragnardb.parser.ast.SelectStatement;
import ragnardb.parser.ast.Statement;

import java.util.ArrayList;

public interface ISQLQueryType extends ISQLTypeBase {
  /**
   * On entire single tables, is able to get the table which is referenced.
   * Note that if the table cannot be found, null will be returned.
   * @param name
   * @return
   */
  public ISQLTableType getTable(String name);

  /**
   * On columns, returns the type of the column. If the column cannot be found, defaults to object.
   * @param name
   * @param tableName
   * @return
   */
  public IType getColumn(String name, String tableName);

  public SQLPlugin getPlugin();

  /**
   * Creates a query result type.
   * @param statement
   * @param type
   * @return
   */
  public ISQLQueryResultType getResults(Statement statement, ISQLQueryType type);

  /**
   * Creates a query result type.
   * @param columns
   * @param statement
   * @return
   */
  public ISQLQueryResultType getResults(ArrayList<SQLColumnPropertyInfo> columns, Statement statement);

  /**
   * Gets column properties; used to handle multiple tables. Defaults to null.
   * @param colName
   * @param tableName
   * @return
   */
  public SQLColumnPropertyInfo getColumnProperty(String colName, String tableName);

  public ISQLQueryResultType getResultType();

  public void setResultType(ISQLQueryResultType type);
}
