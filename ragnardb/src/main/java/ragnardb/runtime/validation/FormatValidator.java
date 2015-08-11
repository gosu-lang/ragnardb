package ragnardb.runtime.validation;

import ragnardb.runtime.FieldValidator;

/**
 * Created by klu on 8/11/2015.
 */
public class FormatValidator<T> extends FieldValidator<T>{
  private String regexp;

  public FormatValidator(String reg){
    regexp = reg;
  }

  public void setRegex(String reg){
    regexp = reg;
  }

  @Override
  public void validateValue(T value) {
    String valueString = value.toString();
    if(!valueString.matches(regexp)){
      throw new ValidationException("Validation exception: expected string format of value to match '" + regexp + "'.");
    }
  }
}
