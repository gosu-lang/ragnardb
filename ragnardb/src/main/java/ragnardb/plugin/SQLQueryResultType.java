package ragnardb.plugin;

import gw.fs.IFile;
import gw.lang.reflect.ITypeInfo;
import gw.util.concurrent.LockingLazyVar;
import ragnardb.parser.ast.SelectStatement;
import ragnardb.parser.ast.Statement;
import ragnardb.runtime.SQLRecord;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by klu on 7/9/2015.
 */
public class SQLQueryResultType extends SQLTypeBase implements ISQLQueryResultType{
  private Statement statement;
  private ISQLQueryType query;
  private SQLRecord record;
  private ArrayList<SQLColumnPropertyInfo> props = null;

  public SQLQueryResultType(IFile file, SQLPlugin plugin, Statement statement, ISQLQueryType type) {
    super(file, plugin);
    this.statement = statement;
    query = type;
    record = null;
  }

  public SQLQueryResultType(IFile file, SQLPlugin plugin, ArrayList<SQLColumnPropertyInfo> propertyInfos, ISQLQueryType t, Statement statement){
    super(file, plugin);
    props = propertyInfos;
    query = t;
    this.statement = statement;
  }

  @Override
  public List<ColumnDefinition> getColumnDefinitions() {
    //TODO: implement this
    return null;
  }

  @Override
  protected SQLBaseTypeInfo initTypeInfo() {
    if(props == null) {
      return new SQLQueryResultTypeInfo((ISQLQueryResultType) getTypeRef(), statement, query);
    } else {
      return new SQLQueryResultTypeInfo(props, (ISQLQueryResultType) getTypeRef());
    }
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

  @Override
  public ISQLTableType getTable(){
    return query.getTable(statement.getTables().get(0));
  }
}
