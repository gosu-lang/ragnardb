package ragnardb.runtime;

import gw.lang.reflect.IType;
import gw.lang.reflect.features.PropertyReference;
import gw.util.GosuExceptionUtil;
import ragnardb.parser.ast.SQL;
import ragnardb.plugin.SQLColumnPropertyInfo;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by carson on 7/1/15.
 */
public class SQLQuery<T> implements Iterable<T> {

  protected ITypeToSQLMetadata _metadata;
  protected IType _rootType;
  private SQLConstraint _whereExpr;
  private SQLConstraint _joinExpr; // Includes On expressions as well!
  private SQLConstraint _orderByExpr;
  private PropertyReference _pick;

  private void addJoin(SQLConstraint cons){
    if(_joinExpr == null){
      _joinExpr = cons;
    }
    else{
      _joinExpr = _joinExpr.addOn(cons);
    }
  }

  public SQLQuery( ITypeToSQLMetadata md, IType rootType )
  {
    _metadata = md;
    _rootType = rootType;
  }

  public SQLQuery<T> where(SQLConstraint constraint) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery._whereExpr = (this._whereExpr == null) ? constraint : this._whereExpr.andAlso( constraint );
    return newQuery;
  }

  public SQLQuery<T> crossJoin( IType type) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.addJoin(SQLConstraint.join(type, "CROSS JOIN"));
    return newQuery;
  }

  public SQLQuery<T> join( IType type) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.addJoin(SQLConstraint.join(type, "JOIN"));
    return newQuery;
  }

  public SQLQuery<T> innerJoin( IType type) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.addJoin(SQLConstraint.join(type, "INNER JOIN"));
    return newQuery;
  }

  public SQLQuery<T> leftOuterJoin( IType type) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.addJoin(SQLConstraint.join(type, "LEFT OUTER JOIN"));
    return newQuery;
  }

  public SQLQuery<T> rightOuterJoin( IType type) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.addJoin(SQLConstraint.join(type, "RIGHT OUTER JOIN"));
    return newQuery;
  }

  public SQLQuery<T> rightJoin( IType type) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.addJoin(SQLConstraint.join(type, "RIGHT JOIN"));
    return newQuery;
  }

  public SQLQuery<T> leftJoin( IType type) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.addJoin(SQLConstraint.join(type, "LEFT JOIN"));
    return newQuery;
  }

  public SQLQuery<T> naturalJoin( IType type) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.addJoin(SQLConstraint.join(type, "NATURAL JOIN"));
    return newQuery;
  }


  public SQLQuery<T> on( SQLConstraint constraint) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.addJoin(SQLConstraint.on(constraint));
    return newQuery;
  }

  public SQLQuery<T> orderBy(SQLConstraint ... constraints){
    SQLQuery<T> newQuery = cloneMe();
    newQuery._orderByExpr = SQLConstraint.orderBy(constraints);
    return newQuery;
  }

  public <U> SQLQuery<U> pick( PropertyReference<Object, U> ref)
  {
    SQLQuery<U> sqlQuery = (SQLQuery<U>) cloneMe();
    sqlQuery._pick = ref;
    return sqlQuery;
  }



  public Iterator<T> iterator()
  {
    return execQuery().iterator();
  }

  public String  getSQLString() {
    String select =  "SELECT " + getSelect();
    String from = "FROM " + _metadata.getTableForType( _rootType );
    String join = _joinExpr == null ? "" : _joinExpr.getSQL( _metadata);
    String where = _whereExpr == null ? "" : "WHERE " + _whereExpr.getSQL( _metadata );
    String orderBy =  _orderByExpr == null ? "" :  _orderByExpr.getSQL(_metadata);
    return select + " " +  from + " "  + join + " " + " " + where + " " + orderBy;
  }

  //--------------------------------------------------------------------------------
  // Implementation
  //--------------------------------------------------------------------------------

  private String getSelect()
  {
    if( _pick != null )
    {
      return ((SQLColumnPropertyInfo)_pick.getPropertyInfo()).getColumnName();
    }
    else
    {
      return _metadata.getTableForType( _rootType ) + ".* ";
    }
  }

  public List<Object> getArgs()
  {
    List answer = new ArrayList();
    if(_joinExpr!=null) {
      answer.addAll(_joinExpr.getArgs());
    }
    if(_whereExpr!=null) {
      answer.addAll(_whereExpr.getArgs());
    }
    return answer;
  }

  private SQLQuery<T> cloneMe()
  {
    SQLQuery<T> child = new SQLQuery<T>( _metadata, _rootType );
    child._metadata = this._metadata;
    child._rootType = this._rootType;
    child._whereExpr = this._whereExpr;
    child._joinExpr = this._joinExpr;
    child._pick = this._pick;
    child._orderByExpr = this._orderByExpr;
    return child;
  }

  private Iterable<T> execQuery()
  {
    try
    {
      if(_pick != null) {
        return SQLRecord.selectSingleColumn( getSQLString(), getArgs() );
      } else {
        return SQLRecord.select( getSQLString(), getArgs(), _rootType );
      }
    }
    catch( SQLException e )
    {
      throw GosuExceptionUtil.forceThrow( e );
    }
  }
}