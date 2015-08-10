package ragnardb.api;


import gw.lang.reflect.features.IPropertyReference;
import ragnardb.runtime.IFieldValidator;

import java.util.List;

public interface IModelConfig
{
  void setTableName(String name);
  String getTableName();

  void setIdColumn(String config);
  String getIdColumn();

  <T> void addValidation( IPropertyReference<Object, T> propertyReference, IFieldValidator<T> validator );

  List<IFieldValidator> getValidatorsForColumn( String columnName );

}
