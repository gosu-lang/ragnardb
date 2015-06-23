package ragnardb.parser.ast;

/**
 * Created by klu on 6/22/2015.
 */
public class CaseTerm implements Term {
  private Case _case;

  public CaseTerm() {
    _case = null;
  }

  public CaseTerm(Case c) {
    _case = c;
  }

  public void setCase(Case c) {
    _case = c;
  }

  public Case getCase() {
    return _case;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("<Term>\n");
    if (_case != null) {
      sb.append(_case);
    }
    return sb.toString();
  }

  public void setNegative(boolean isNeg) {
  }

}
