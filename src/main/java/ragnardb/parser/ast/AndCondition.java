package ragnardb.parser.ast;

import java.util.ArrayList;

/**
 * Created by klu on 6/22/2015.
 */
public class AndCondition {
  private ArrayList<Condition> _conditions;

  public AndCondition(){_conditions = new ArrayList<Condition>();}

  public AndCondition(Condition c){
    _conditions = new ArrayList<Condition>();
    _conditions.add(c);
  }

  public void addCondition(Condition c){_conditions.add(c);}

  public ArrayList<Condition> getConditions(){return _conditions;}

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder("<AndCondition>\n");
    for(Condition c: _conditions){
      sb.append('\t');
      sb.append(c);
    }
    return sb.toString();
  }
}
