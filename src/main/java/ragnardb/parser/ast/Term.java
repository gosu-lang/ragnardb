package ragnardb.parser.ast;

/**
 * Created by klu on 6/22/2015.
 */
public abstract class Term {

  @Override
  public abstract String toString();

  public abstract void setNegative(boolean isNeg);

  protected abstract String toString(String initial);
}
