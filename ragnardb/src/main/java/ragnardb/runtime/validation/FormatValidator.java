package ragnardb.runtime.validation;

import ragnardb.runtime.FieldValidator;

/**
 * Created by klu on 8/11/2015.
 */
public class FormatValidator<T> extends FieldValidator<T>{
  private String regexp;
  private int min = -1, max = -1;

  public FormatValidator(String reg){
    regexp = reg;
  }

  public FormatValidator(int min, int max){
    regexp = null;
    this.min = min;
    this.max = max;
  }

  public void setRegex(String reg){
    regexp = reg;
  }

  public void setMinAndMax(int min, int max){
    this.min = min;
    this.max = max;
  }

  @Override
  public void validateValue(T value) {
    if(value == null){
      throw new ValidationException("Validation Exception: Attempted to apply validation test to null object");
    }
    String valueString = value.toString();
    if(regexp != null) {
      if (!valueString.matches(regexp)) {
        throw new ValidationException("Validation exception: expected string format of value to match '" + regexp + "'.");
      }
    }
    if(valueString.length()<min){
      throw new ValidationException("Validation exception: string must have length between " + min + " and " + max + ".");
    }
    if(max>=0){
      if(valueString.length()>max){
        throw new ValidationException("Validation exception: string must have length between " + min + " and " + max + ".");
      }
    }
  }
}
