package ragnardb.parser.ast;

/**
 * Created by klu on 6/22/2015.
 */
public abstract class Term {

  @Override
  public abstract String toString();

  public abstract void setNegative(boolean isNeg);

  protected abstract String toString(String initial);

  public String getName(){return "This is not a variable :(";}

  public String getType(){return "This is not a variable :(";}
}
