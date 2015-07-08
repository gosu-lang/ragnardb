package ragnardb.plugin;

import gw.fs.IFile;

import java.util.ArrayList;
import java.util.Map;

public class SQLQueryType extends SQLTypeBase implements ISQLQueryType {
  public SQLQueryType(IFile file, SQLPlugin plugin) {
    super(file, plugin);
  }

  public ISQLTableType getTable(String name){
    return _plugin.getTypeFromRelativeName(name, this.getNamespace());
  }

  @Override
  protected SQLBaseTypeInfo initTypeInfo() {
    return new SQLQueryTypeInfo((ISQLQueryType) getTypeRef());
  }
}
