package ragnardb.parser.ast;

import ragnardb.parser.Token;

import java.util.ArrayList;

/**
 * Created by klu on 7/7/2015.
 */
public class UpdateStatement extends Statement{
  private String _tableName;
  private ArrayList<String> _columns;
  private ArrayList<Token> _tokens;
  private ArrayList<JavaVar> _vars;

  public UpdateStatement(String name){
    _tableName = name;
    _columns = new ArrayList<>();
    _tokens = new ArrayList<>();
    _vars = new ArrayList<>();
  }

  public void setTableName(String name){_tableName = name;}

  public void addColumn(String name){_columns.add(name);}

  public String getTableName(){return _tableName;}

  public ArrayList<String> getColumns(){return _columns;}

  public void addToken(Token t){_tokens.add(t);}

  public ArrayList<Token> getTokens(){return _tokens;}

  public ArrayList<JavaVar> getVariables(){return _vars;}

  public void addVar(JavaVar v){_vars.add(v);}

  public void setVars(ArrayList<JavaVar> v){_vars = v;}

  public ArrayList<ResultColumn> getResultColumns(){
    return null;
  }

  public ArrayList<String> getTables(){
    ArrayList<String> e = new ArrayList<>();
    e.add(_tableName);
    return e;
  }

}
