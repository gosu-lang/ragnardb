package ragnardb.parser.ast;

import ragnardb.plugin.ColumnDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjennings on 6/23/2015.
 */
public class CreateTable {
  private List<ColumnDefinition> columns;
  private List<Constraint> constraints;
  private String name;
  //private List<Constraint> Constraints;

  public CreateTable(String _name){
    columns = new ArrayList<ColumnDefinition>();
    constraints = new ArrayList<>();
    name = _name;
  }
  public void append(ColumnDefinition c){
    columns.add(c);
  }

  public void append(Constraint c){
    constraints.add(c);
  }

  public List<ColumnDefinition> getColumnDefinitions(){
    return columns;
  }
  public List<Constraint> getConstraints(){
    return constraints;
  }

  public String getName(){
    return name;
  }

  public ColumnDefinition getColumnDefinitionByName(String name){
    for(ColumnDefinition col : this.getColumnDefinitions()){
      if(col.getColumnName().equals(name)){
        return col;
      }
    }
    return null;
  }


}
