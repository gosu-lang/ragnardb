package ragnardb.plugin;

import gw.lang.reflect.IFileBasedType;
import gw.lang.reflect.IType;

import java.util.List;

public interface ISqlTableType extends IType, IFileBasedType {
  List<ColumnDefinition> getColumnDefinitions();
}
