package ragnardb.runtime.validation;

import ragnardb.plugin.SQLColumnPropertyInfo;
import ragnardb.runtime.FieldValidator;
import ragnardb.runtime.SQLConstraint;
import ragnardb.runtime.SQLQuery;

import java.util.Collections;

/**
 * Created by klu on 8/11/2015.
 */
public class UniqueValidator<T> extends FieldValidator<T> {
  private SQLColumnPropertyInfo propInfo;
  private String tableName;


  public UniqueValidator(SQLColumnPropertyInfo prop, String t){
    propInfo = prop;
    tableName = t;
  }

  @Override
  public void validateValue(T value) {
    if(value == null){
      throw new ValidationException("Validation Exception: Attempted to apply validation test to null object");
    }
    ValidationQuery query = new ValidationQuery("SELECT * FROM " + tableName + " WHERE " + propInfo.getColumnName() + " = ?", Collections.singletonList(value));
    if(!query.unique()){
      throw new ValidationException("Validation Exception: Unique value required for '" + propInfo.getName() + "'.");
    }
  }
}
