package ragnardb.runtime;

/**
 * Created by klu on 8/11/2015.
 */
public abstract class FieldValidator<T> implements IFieldValidator<T>{

  @Override
  public abstract void validateValue(T value);
}
