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

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder("<Insert/Replace>\nINTO\n");
    sb.append(_tableName + "\n");
    if(_columns.size()!=0){
      sb.append("\t<Columns>\n");
      for(String columnname: _columns){
        sb.append("\t"+columnname+"\n");
      }
    }
    if(_select == null){
      sb.append("\t<Values>\n");
      for(Expression expr: _expressions){
        sb.append(expr.toString("\t"));
      }
    } else {
      sb.append("\t<Values as Select>\n");
      sb.append(_select.toString("\t"));
    }
    return sb.toString();
  }

  protected String toString(String initial){
    StringBuilder sb = new StringBuilder(initial+"<Insert/Replace>\n"+initial+"INTO\n");
    sb.append(initial+_tableName + "\n");
    if(_columns.size()!=0){
      sb.append(initial+"\t<Columns>\n");
      for(String columnname: _columns){
        sb.append(initial+"\t"+columnname+"\n");
      }
    }
    if(_select == null){
      sb.append(initial+"\t<Values>\n");
      for(Expression expr: _expressions){
        sb.append(expr.toString(initial+"\t"));
      }
    } else {
      sb.append(initial+"\t<Values as Select>\n");
      sb.append(_select.toString(initial+"\t"));
    }
    return sb.toString();
  }
}
