package ragnardb.parser.ast;

import java.util.ArrayList;

/**
 * Created by klu on 6/22/2015.
 */
public class Case {
  private Expression _initial;

  private class WhenThen {
    private Expression _when;
    private Expression _then;

    private WhenThen(Expression w, Expression t){
      _when = w;
      _then = t;
    }
  }
  private ArrayList<WhenThen> _whenThens;

  private Expression _else;

  public Case() {
    _initial = null;
    _whenThens = new ArrayList<WhenThen>();
    _else = null;
  }

  public Case(Expression init, ArrayList<WhenThen> wt, Expression el){
    _initial = init;
    _whenThens = wt;
    _else = el;
  }

  public void setInitial(Expression init){
    _initial = init;
  }

  public void addWhenThen(Expression when, Expression then){
    _whenThens.add(new WhenThen(when, then));
  }

  public void setElse(Expression el){
    _else = el;
  }

  public Expression getInitial(){
    return _initial;
  }

  public ArrayList<Expression[]> getWhenThenClauses(){
    ArrayList<Expression[]> clauses = new ArrayList<Expression[]>();
    for(WhenThen wt: _whenThens){
      Expression[] currentClause = {wt._when, wt._then};
      clauses.add(currentClause);
    }
    return clauses;
  }

  public Expression getElse(){
    return _else;
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder("<Case>\n");
    if(_initial != null){
      sb.append('\t');
      sb.append(_initial);
    }
    for(WhenThen wt: _whenThens){
      sb.append("\tWHEN ");
      sb.append(wt._when);
      sb.append("\tTHEN ");
      sb.append(wt._then);
    }
    if(_else != null){
      sb.append("\tELSE ");
      sb.append(_else);
    }
    return sb.toString();
  }

}
