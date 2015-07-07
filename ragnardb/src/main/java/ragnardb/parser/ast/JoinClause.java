package ragnardb.parser.ast;

import javafx.scene.control.Tab;

import java.util.ArrayList;

/**
 * Created by klu on 7/6/2015.
 */
public class JoinClause {
  private ArrayList<TableOrSubquery> tables;
  private ArrayList<String> joins;
  private ArrayList<JoinConstraint> constraints;


  private class JoinConstraint {
    private Expression expr;
    private ArrayList<String> columnNames;

    private String toString(String initial){
      if(expr!=null){
        return initial + "ON\n" + expr.toString(initial + "\t");
      } else if(columnNames != null){
        return initial + "USING" + String.join(", ", columnNames) + "\n";
      } else {
        return "";
      }
    }
  }

  public JoinClause(){
    tables = new ArrayList<>();
    joins = new ArrayList<>();
    constraints = new ArrayList<>();
  }

  public JoinClause(TableOrSubquery t){
    tables = new ArrayList<>();
    joins = new ArrayList<>();
    constraints = new ArrayList<>();
    tables.add(t);
  }

  public void add(TableOrSubquery t, String j, Expression e){
    tables.add(t);
    joins.add(j);
    JoinConstraint c = new JoinConstraint();
    c.expr = e;
    c.columnNames = null;
    constraints.add(c);
  }

  public void add(TableOrSubquery t, String j, ArrayList<String> cols){
    tables.add(t);
    joins.add(j);
    JoinConstraint c = new JoinConstraint();
    c.expr = null;
    c.columnNames = cols;
    constraints.add(c);
  }

  public void add(TableOrSubquery t, String j){
    tables.add(t);
    joins.add(j);
    JoinConstraint c = new JoinConstraint();
    c.expr = null;
    c.columnNames = null;
    constraints.add(c);
  }

  public ArrayList<String> getNames(){
    ArrayList<String> names = new ArrayList<>();
    for(TableOrSubquery t: tables){
      names.add(t.getName());
    }
    return names;
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder("<Join-Clause>\n");
    sb.append(tables.get(0));
    for(int i = 0; i < joins.size(); i++){
      sb.append("\t" + joins.get(i) + "\n");
      sb.append(tables.get(i+1));
      sb.append("\t" + constraints.get(i).toString("") + "\n");
    }
    return sb.toString();
  }

  protected String toString(String initial){
    StringBuilder sb = new StringBuilder(initial+"<Join-Clause>\n");
    sb.append(tables.get(0).toString(initial+"\t"));
    for(int i = 0; i < joins.size(); i++){
      sb.append(initial + "\t" + joins.get(i) + "\n");
      sb.append(tables.get(i+1).toString(initial+"\t"));
      sb.append(initial + "\t" + constraints.get(i).toString("") + "\n");
    }
    return sb.toString();
  }

}
