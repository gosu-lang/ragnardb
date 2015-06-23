package ragnardb.parser.ast;

/**
 * Created by klu on 6/22/2015.
 */
public class GeneralTerm extends Term {
  private SelectStatement _select;
  private Expression _expression;
  private boolean isNegative;

  public GeneralTerm() {
  }

  public GeneralTerm(SelectStatement s) {
    _select = s;
    _expression = null;
  }

  public GeneralTerm(Expression e) {
    _expression = e;
    _select = null;
  }

  public Expression getExpression() {
    return _expression;
  }

  public void setExpression(Expression e) {
    _expression = e;
  }

  public SelectStatement getSelect() {
    return _select;
  }

  public void setSelect(SelectStatement s) {
    _select = s;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("<Term>\n");
    if (_expression != null) {
      sb.append("\t");
      sb.append(_expression);
    }
    if (_select != null) {
      sb.append("\t");
      sb.append(_select);
    }
    return sb.toString();
  }

  public void setNegative(boolean isNeg) {
    isNegative = isNeg;
  }

  protected String toString(String initial) {
    StringBuilder sb = new StringBuilder(initial + "<Term>\n");
    if (_expression != null) {
      sb.append(_expression.toString(initial + "\t"));
    }
    if (_select != null) {
      sb.append("\t");
      sb.append(_select);
    }
    return sb.toString();
  }

}
