package ragnardb.api;


import gw.lang.reflect.features.IPropertyReference;
import ragnardb.runtime.IFieldValidator;
import ragnardb.runtime.SQLRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IModelConfig
{
  void setTableName(String name);
  String getTableName();

  void setIdColumn(String config);
  String getIdColumn();

  <T> void addValidation( IPropertyReference<Object, T> propertyReference, IFieldValidator<T> validator );
  <T> void validateFormat(IPropertyReference<Object, T> propertyReference, String regexp);
  <T> void requiredFields(List<IPropertyReference<Object, T>> propertyReferences);
  <T> void lengthBetween(IPropertyReference<Object, T> propertyReference, int minlength, int maxlength);
  <T> void unique(IPropertyReference<Object, T> propertyReference);
  <T> void hasContent(IPropertyReference<Object, T> propertyReference);
  <T> void isInSet(IPropertyReference<Object, T> propertyReference, List<Object> objs);
  <T> void isInSet(IPropertyReference<Object, T> propertyReference, Set<Object> objs);

  void clearValidators();
  Map<String, List<String>> getErrorsList();
  List<IPropertyReference> getPropertyReferences();

  boolean isValidModifyingErrors (SQLRecord sqlRecord);
  boolean isValid( SQLRecord sqlRecord );
}
