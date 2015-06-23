package ragnardb.parser.ast;

/**
 * Created by klu on 6/22/2015.
 */
public class Condition {
  /*We express condition as a relationship between two operands, regardless of what the relationship is*/
  private Operand first, second;

  public Condition(){
    first = null;
    second = null;
  }

  public Condition(Operand o1, Operand o2){
    first = o1;
    second = o2;
  }

  public Condition(Operand o){
    first = o;
    second = null;
  }

  public void setFirst(Operand o){first = o;}

  public void setSecond(Operand o){second = o;}

  public Operand getFirst(){return first;}

  public Operand getSecond(){return second;}

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder("<Condition>\n\t");
    sb.append(first);
    if(second != null){
      sb.append('\t');
      sb.append(second);
    }
    return sb.toString();
  }
}
