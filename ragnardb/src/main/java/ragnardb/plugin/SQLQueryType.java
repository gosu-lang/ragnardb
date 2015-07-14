package ragnardb.plugin;

import gw.fs.IFile;
import gw.lang.reflect.IType;
import ragnardb.parser.ast.SelectStatement;

import java.util.ArrayList;
import java.util.Map;

public class SQLQueryType extends SQLTypeBase implements ISQLQueryType {
  public SQLQueryType(IFile file, SQLPlugin plugin) {
    super(file, plugin);
  }

  public ISQLTableType getTable(String name){
    return _plugin.getTypeFromRelativeName(name.toLowerCase(), this.getNamespace());
  }

  public SQLPlugin getPlugin() {return _plugin;}

  public IType getColumn(String name){
    return _plugin.getColumnFromRelativeName(name, this.getNamespace());
  }

  public ISQLQueryResultType getResults(SelectStatement statement, ISQLQueryType type){
    return new SQLQueryResultType(this.getFile(), this._plugin, statement, type);
  }

  public ISQLQueryResultType getResultType(){
    SQLQueryTypeInfo typeinfo = (SQLQueryTypeInfo) initTypeInfo();
    return typeinfo.getResultType(this);
  }

  @Override
  protected SQLBaseTypeInfo initTypeInfo() {
    return new SQLQueryTypeInfo((ISQLQueryType) getTypeRef());
  }
}
