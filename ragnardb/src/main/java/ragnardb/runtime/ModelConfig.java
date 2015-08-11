package ragnardb.runtime;

import gw.lang.reflect.features.IPropertyReference;
import ragnardb.api.IModelConfig;
import ragnardb.plugin.SQLColumnPropertyInfo;
import ragnardb.runtime.validation.ContentValidator;
import ragnardb.runtime.validation.FormatValidator;
import ragnardb.runtime.validation.UniqueValidator;
import ragnardb.runtime.validation.ValidationException;

import java.util.*;


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

  /*Allows validation by required*/
  public <T> void requiredFields(List<IPropertyReference<Object, T>> propertyReferences){
    for(IPropertyReference<Object, T> propertyReference: propertyReferences){
      SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
      List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
      if( validators == null )
      {
        validators = new ArrayList<>();
      }
      validators.add(obj -> {if(obj == null){throw new ValidationException("Validation Exception: Object cannot be null");}});
      _validatorsByField.put(propertyInfo.getColumnName(), validators);
    }
  }

  /*Allows validation by length, set maxlength to -1 to have no maximum length*/
  public <T> void lengthBetween(IPropertyReference<Object, T> propertyReference, int minlength, int maxlength){
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add(new FormatValidator<T>(minlength, maxlength));
    _validatorsByField.put(propertyInfo.getColumnName(), validators);
  }

  public <T> void unique(IPropertyReference<Object, T> propertyReference){
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add(new UniqueValidator<T>(propertyInfo, _tableName));
    _validatorsByField.put(propertyInfo.getColumnName(), validators);
  }

  public <T> void hasContent(IPropertyReference<Object, T> propertyReference){
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add(new FormatValidator<T>(1, -1));
    _validatorsByField.put(propertyInfo.getColumnName(), validators);
  }

  /*Misnomer; we actually take a list object*/
  public <T> void isInSet(IPropertyReference<Object, T> propertyReference, List<Object> objs){
    isInSet(propertyReference, new HashSet<Object>(objs));
  }

  public <T> void isInSet(IPropertyReference<Object, T> propertyReference, Set<Object> objs){
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add(new ContentValidator<T>(objs));
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

  public void clearValidators(){
    _validatorsByField.clear();
  }
}
