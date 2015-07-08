package ragnardb.parser.ast;

import ragnardb.parser.Token;

import java.util.ArrayList;

/**
 * Created by klu on 7/7/2015.
 */
public class UpdateStatement extends SQL{
  private String _tableName;
  private ArrayList<String> _columns;
  private ArrayList<Token> _tokens;

  public UpdateStatement(String name){
    _tableName = name;
    _columns = new ArrayList<>();
    _tokens = new ArrayList<>();
  }

  public void setTableName(String name){_tableName = name;}

  public void addColumn(String name){_columns.add(name);}

  public String getTableName(){return _tableName;}

  public ArrayList<String> getColumns(){return _columns;}

  public void addToken(Token t){_tokens.add(t);}

  public ArrayList<Token> getTokens(){return _tokens;}
}
