package ragnardb.parser.ast;

/**
 * Created by klu on 6/22/2015.
 */
public interface Term {

  @Override
  public String toString();

  public void setNegative(boolean isNeg);
}
