package ragnardb.parser.ast;

/**
 * Created by klu on 7/7/2015.
 */
public class DeleteStatement extends SQL{
  private String _tableName;
  private Expression _expr;
  private Term _term;

  public String getTableName() {
    return _tableName;
  }

  public void setTableName(String _tableName) {
    this._tableName = _tableName;
  }

  public Expression getExpr() {
    return _expr;
  }

  public void setExpr(Expression _expr) {
    this._expr = _expr;
  }

  public Term getTerm() {
    return _term;
  }

  public void setTerm(Term _term) {
    this._term = _term;
  }
}
