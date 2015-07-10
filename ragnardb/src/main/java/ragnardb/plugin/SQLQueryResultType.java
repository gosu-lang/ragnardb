package ragnardb.plugin;

import gw.fs.IFile;
import ragnardb.parser.ast.SelectStatement;

import java.util.List;

/**
 * Created by klu on 7/9/2015.
 */
public class SQLQueryResultType extends SQLTypeBase implements ISQLQueryResultType{
  private SelectStatement statement;
  private ISQLQueryType query;


  public SQLQueryResultType(IFile file, SQLPlugin plugin, SelectStatement statement, ISQLQueryType type) {
    super(file, plugin);
    this.statement = statement;
    query = type;
  }

  @Override
  public List<ColumnDefinition> getColumnDefinitions() {
    //TODO: implement this
    return null;
  }

  @Override
  protected SQLBaseTypeInfo initTypeInfo() {
    return new SQLQueryResultTypeInfo((ISQLQueryResultType) getTypeRef(), statement, query);
  }

  @Override
  public String getName() {
    return query.getName()+"Result";
  }

  @Override
  public String getRelativeName() {
    return query.getRelativeName()+"Result";
  }

  @Override
  public String getNamespace() {
    return query.getNamespace();
  }
}
