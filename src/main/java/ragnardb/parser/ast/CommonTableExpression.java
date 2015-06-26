package ragnardb.parser.ast;

import java.util.ArrayList;

/**
 * Created by klu on 6/25/2015.
 */
public class CommonTableExpression {
  private String name;
  private ArrayList<String> columns;
  private SelectStatement select;

  public CommonTableExpression(String s){
    name = s;
    columns = new ArrayList<>();
    select = null;
  }

  public void addColumn(String s){
    columns.add(s);
  }

  public void setName(String s){
    name = s;
  }

  public void setSelect(SelectStatement s){
    select = s;
  }

  public String getName(){return name;}

  public ArrayList<String> getColumns(){return columns;}

  public String toString(){
    StringBuilder sb = new StringBuilder("<CommonTableExpression>");
    sb.append(name);
    for(String s: columns){
      sb.append(" " + s);
    }
    sb.append("\n");
    sb.append("\t" + select);
    return sb.toString();
  }

  protected String toString(String initial){
    StringBuilder sb = new StringBuilder(initial + "<CommonTableExpression>");
    sb.append(name);
    for(String s: columns){
      sb.append(" " + s);
    }
    sb.append("\n");
    sb.append(select.toString(initial+"\t"));
    return sb.toString();
  }
}
