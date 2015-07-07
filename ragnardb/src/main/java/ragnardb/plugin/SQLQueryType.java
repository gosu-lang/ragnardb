package ragnardb.plugin;

import gw.fs.IFile;

public class SQLQueryType extends SQLTypeBase implements ISQLQueryType {
  public SQLQueryType(IFile file, SQLPlugin plugin) {
    super(file, plugin);
  }

  @Override
  protected SQLBaseTypeInfo initTypeInfo() {
    return new SQLQueryTypeInfo((ISQLQueryType) getTypeRef());
  }
}
