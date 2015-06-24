package ragnardb.parser.ast;

import ragnardb.plugin.ColumnDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjennings on 6/23/2015.
 */
public class CreateTable {
  private List<ColumnDefinition> columns;
  private String name;
  //private List<Constraint> Constraints;
  public CreateTable(String _name){
    columns = new ArrayList<ColumnDefinition>();
    name = _name;
  }
  public void append(ColumnDefinition c){
    columns.add(c);
  }
  public List<ColumnDefinition> getColumnDefnitions(){
    return columns;
  }
  public String getName(){
    return name;
  }


}
