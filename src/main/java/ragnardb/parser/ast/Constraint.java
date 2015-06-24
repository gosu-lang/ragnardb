package ragnardb.parser.ast;

/**
 * Created by klu on 6/23/2015.
 */
public abstract class Constraint {

  public abstract void setExpression(Expression e);

  public abstract void addColumnName(String s);

  public abstract void setName(String s);

  public abstract String getName();

  @Override
  public abstract String toString();

  protected abstract String toString(String initial);

}
