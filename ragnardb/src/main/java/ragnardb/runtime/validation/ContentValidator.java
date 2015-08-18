package ragnardb.runtime.validation;

import ragnardb.runtime.FieldValidator;

import java.util.Set;

/**
 * Created by klu on 8/11/2015.
 */
public class ContentValidator<T> extends FieldValidator<T>{
  private Set<Object> objs;

  public ContentValidator(Set<Object> ob){
    objs = ob;
  }

  @Override
  public void validateValue(T value) {
    if(value == null){
      throw new ValidationException("Validation Exception: Attempted to apply validation test to null object");
    }
    if(!objs.contains(value)){
      throw new ValidationException("Validation Exception: Value is not in accepted list of values");
    }
  }
}
