package ragnardb.parser.ast;

import gw.lang.parser.resources.Res;
import ragnardb.parser.Token;

import java.util.ArrayList;

/**
 * Created by klu on 7/7/2015.
 */
public class InsertStatement extends Statement{
  private String _tableName;
  private ArrayList<String> _columns;
  private ArrayList<Expression> _expressions;
  private ArrayList<Token> _tokens;
  private SelectStatement _select;
  private ArrayList<JavaVar> _vars;

  public InsertStatement(String name){
    _tableName = name;
    _columns = new ArrayList<>();
    _expressions = new ArrayList<>();
    _tokens = new ArrayList<>();
    _select = null;
    _vars = new ArrayList<>();
  }

  public void setIsSelect(boolean b){
    if(b){
      _expressions = null;
    } else {
      _select = null;
      _expressions = new ArrayList<>();
    }
  }

  public String getTableName() {
    return _tableName;
  }

  public void setTableName(String _tableName) {
    this._tableName = _tableName;
  }

  public ArrayList<String> getColumns() {
    return _columns;
  }

  public void addColumn(String col){_columns.add(col);}

  public void setColumns(ArrayList<String> _columns) {
    this._columns = _columns;
  }

  public ArrayList<Expression> getExpressions() {
    return _expressions;
  }

  public void addExpression(Expression e){_expressions.add(e);}

  public void setExpressions(ArrayList<Expression> _expressions) {
    this._expressions = _expressions;
  }

  public ArrayList<Token> getTokens() {
    return _tokens;
  }

  public void addToken(Token t){_tokens.add(t);}

  public void setTokens(ArrayList<Token> _tokens) {
    this._tokens = _tokens;
  }

  public SelectStatement getSelect() {
    return _select;
  }

  public void setSelect(SelectStatement _select) {
    this._select = _select;
  }

  public ArrayList<JavaVar> getVariables() {
    return _vars;
  }

  public void addVar(JavaVar v){_vars.add(v);}

  public void setVars(ArrayList<JavaVar> _vars) {
    this._vars = _vars;
  }

  public void setDefault(int i){
    for(int j = 0; j<i; j++){
      addExpression(new Default());
    }
  }

  public ArrayList<ResultColumn> getResultColumns(){
    return null;
  }

  public ArrayList<String> getTables(){
    ArrayList<String> e = new ArrayList<>();
    e.add(_tableName);
    return e;
  }
}
