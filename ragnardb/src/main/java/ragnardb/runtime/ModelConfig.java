package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.features.IPropertyReference;
import ragnardb.api.IModelConfig;
import ragnardb.plugin.SQLColumnPropertyInfo;
import ragnardb.runtime.validation.FormatValidator;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ModelConfig implements IModelConfig
{
  private final List<String> _columns;
  private String _tableName;
  private String _idColumn;
  private Map<String, List<IFieldValidator>> _validatorsByField = new HashMap<>();

  public ModelConfig( String tableName, String idColumn, List<String> columns )
  {
    setTableName( tableName );
    setIdColumn( idColumn );
    _columns = columns;
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


  /*Allows validation by regex*/
  public <T> void validateFormat(IPropertyReference<Object, T> propertyReference, String regexp){
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add(new FormatValidator<T>(regexp));
    _validatorsByField.put(propertyInfo.getColumnName(), validators);
  }



  @Override
  public boolean isValid( SQLRecord sqlRecord )
  {
    Map<String, List<String>> errors = getErrors(sqlRecord);
    return errors.isEmpty();
  }

  private Map<String, List<String>> getErrors( SQLRecord sqlRecord )
  {
    Map<String, List<String>> errors = new HashMap<>();
    for( String column : _columns )
    {
      List<IFieldValidator> validatorsForColumn = getValidatorsForColumn( column );
      for( IFieldValidator validator : validatorsForColumn )
      {
        try
        {
          validator.validateValue( sqlRecord.getRawValue( column ) );
        }
        catch( Exception e )
        {
          List<String> errorList = errors.get( column );
          if( errorList == null )
          {
            errorList = new ArrayList<>();
            errors.put( column, errorList );
          }
          errorList.add( e.getLocalizedMessage() );
        }
      }
    }
    return errors;
  }

  private List<IFieldValidator> getValidatorsForColumn( String columnName )
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
