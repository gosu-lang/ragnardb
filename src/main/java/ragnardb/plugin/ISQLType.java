package ragnardb.plugin;

import gw.lang.reflect.IType;

import java.util.List;

public interface ISQLType extends IType {
  List<ColumnDefinition> getColumnDefinitions();
}
