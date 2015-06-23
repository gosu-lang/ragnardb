package ragnardb.parser.ast;

import java.util.ArrayList;

/**
 * Created by klu on 6/22/2015.
 */
public class Operand {
  private ArrayList<Summand> _summands;

  public Operand(){_summands = new ArrayList<Summand>();}

  public Operand(Summand s){
    _summands = new ArrayList<Summand>();
    _summands.add(s);
  }

  public void addSummand(Summand s){_summands.add(s);}

  public ArrayList<Summand> getSummands(){return _summands;}

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder("<Operand>\n");
    for(Summand s: _summands){
      sb.append('\t');
      sb.append(s);
    }
    return sb.toString();
  }
}
