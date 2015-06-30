package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;

public interface ITypeToSQLMetadata
{
  String getTableForType(IType type);
  String getColumnForProperty(IPropertyInfo pi);
}
