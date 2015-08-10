package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.features.IPropertyReference;
import ragnardb.api.IModelConfig;
import ragnardb.plugin.SQLColumnPropertyInfo;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ModelConfig implements IModelConfig
{
  private String _tableName;
  private String _idColumn;
  private Map<String, List<IFieldValidator>> _validatorsByField = new HashMap<>();

  public ModelConfig( String tableName, String idColumn )
  {
    setTableName( tableName );
    setIdColumn( idColumn );
  }

  public String getTableName()
  {
    return _tableName;
  }

  public String getIdColumn()
  {
    return _idColumn;
  }

  public void setTableName( String tableName )
  {
    _tableName = tableName;
  }

  public void setIdColumn( String idColumn )
  {
    _idColumn = idColumn;
  }

  public <T> void addValidation(IPropertyReference<Object, T> propertyReference, IFieldValidator<T> validator)
  {
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add( validator );
    _validatorsByField.put( propertyInfo.getColumnName(), validators );
  }

  public List<IFieldValidator> getValidatorsForColumn( String columnName )
  {
    List<IFieldValidator> validators = _validatorsByField.get( columnName );
    if( validators == null )
    {
      return Collections.emptyList();
    }
    else
    {
      return validators;
    }
  }
}
