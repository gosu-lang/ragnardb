package ragnardb.runtime;

import gw.lang.reflect.features.IPropertyReference;
import gw.lang.reflect.features.PropertyReference;
import javafx.util.Pair;
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
  private Map<String, List<String>> errorsList = new HashMap<>();
  private List<IPropertyReference> propertyReferences = new ArrayList<>();

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
    propertyReferences.add(propertyReference);
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add( validator );
    _validatorsByField.put( propertyInfo.getColumnName(), validators );
  }


  /**
   * Validation by regex
   * @param propertyReference a property reference
   * @param regexp a regex
   * @param <T>
   */
  public <T> void validateFormat(IPropertyReference<Object, T> propertyReference, String regexp){
    propertyReferences.add(propertyReference);
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add(new FormatValidator<T>(regexp));
    _validatorsByField.put(propertyInfo.getColumnName(), validators);
  }

  /**
   * NOT NULL Validation
   * @param propertyReferenceList list of properties you want not to be null
   * @param <T>
   */
  public <T> void requiredFields(List<IPropertyReference<Object, T>> propertyReferenceList){
    for(IPropertyReference<Object, T> propertyReference: propertyReferenceList){
      propertyReferences.add(propertyReference);
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

  /**
   * For convenience; see other requiredFields method
   * @param propertyReferenceList
   * @param <T>
   */
  public <T> void requiredFields(IPropertyReference<Object, T> ... propertyReferenceList){
    List<IPropertyReference<Object, T>> propList = Arrays.asList(propertyReferenceList);
    requiredFields(propList);
  }

  /**
   * Validation by length of string representation. To specify no maximum, enter -1.
   * @param propertyReference
   * @param minlength
   * @param maxlength
   * @param <T>
   */
  public <T> void lengthBetween(IPropertyReference<Object, T> propertyReference, int minlength, int maxlength){
    propertyReferences.add(propertyReference);
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add(new FormatValidator<T>(minlength, maxlength));
    _validatorsByField.put(propertyInfo.getColumnName(), validators);
  }

  /**
   * UNIQUE Validation; will not work against unsaved data.
   * @param propertyReference
   * @param <T>
   */
  public <T> void unique(IPropertyReference<Object, T> propertyReference){
    propertyReferences.add(propertyReference);
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add(new UniqueValidator<T>(propertyInfo, _tableName));
    _validatorsByField.put(propertyInfo.getColumnName(), validators);
  }

  /**
   * Equivalent to calling lengthBetween with (1,-1).
   * @param propertyReference
   * @param <T>
   */
  public <T> void hasContent(IPropertyReference<Object, T> propertyReference){
    propertyReferences.add(propertyReference);
    SQLColumnPropertyInfo propertyInfo = (SQLColumnPropertyInfo)propertyReference.getPropertyInfo();
    List<IFieldValidator> validators = _validatorsByField.get( propertyInfo.getColumnName() );
    if( validators == null )
    {
      validators = new ArrayList<>();
    }
    validators.add(new FormatValidator<T>(1, -1));
    _validatorsByField.put(propertyInfo.getColumnName(), validators);
  }

  /**
   * See other isInSet
   * @param propertyReference
   * @param objs
   * @param <T>
   */
  public <T> void isInSet(IPropertyReference<Object, T> propertyReference, List<Object> objs){
    isInSet(propertyReference, new HashSet<Object>(objs));
  }

  /**
   * Tests if a property reference has a value equal to a given set.
   * @param propertyReference
   * @param objs
   * @param <T>
   */
  public <T> void isInSet(IPropertyReference<Object, T> propertyReference, Set<Object> objs){
    propertyReferences.add(propertyReference);
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

  @Override
  /*This WILL modify the saved list of errors*/
  public boolean isValidModifyingErrors( SQLRecord sqlRecord ){
    errorsList = getErrors(sqlRecord);
    return errorsList.isEmpty();
  }

  public Map<String, List<String>> getErrorsList(){return errorsList;}

  public List<IPropertyReference> getPropertyReferences(){return propertyReferences;}

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
    List<IFieldValidator> validators = _validatorsByField.get(columnName);
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
