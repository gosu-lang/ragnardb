package ragnardb.plugin;

import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.IType;
import ragnardb.parser.ast.SelectStatement;

public interface ISQLQueryType extends ISQLTypeBase {
  public ISQLTableType getTable(String name);

  public IType getColumn(String name);

  public SQLPlugin getPlugin();

  public ISQLQueryResultType getResults(SelectStatement statement, ISQLQueryType type);

  public ISQLQueryResultType getResultType();
}
