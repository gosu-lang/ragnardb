package ragnardb.runtime;

import gw.lang.reflect.IType;
import gw.util.GosuExceptionUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by carson on 7/1/15.
 */
public class SQLQuery<T> implements Iterable<T>{

  protected IType _rootType;
  private SQLConstraint _whereExpr;
  private SQLConstraint _joinExpr;
  private SQLConstraint _onExpr;
  private SQLQuery _parent;
  protected ITypeToSQLMetadata _metadata;

  public SQLQuery( ITypeToSQLMetadata md, IType rootType )
  {
    _metadata = md;
    _rootType = rootType;
  }



  public SQLQuery<T> where(SQLConstraint constraint) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._whereExpr = constraint;
    newQuery._joinExpr = this._joinExpr; //Carrying data over
    newQuery._onExpr = this._onExpr; //Carrying data over
    return newQuery;
  }

  public SQLQuery<T> crossJoin( IType type) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._joinExpr =  SQLConstraint.join(type , "CROSS JOIN");
    return newQuery;
  }

  public SQLQuery<T> join( IType type) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._joinExpr =  SQLConstraint.join(type , "JOIN");
    return newQuery;
  }

  public SQLQuery<T> innerJoin( IType type) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._joinExpr =  SQLConstraint.join(type , "INNER JOIN");
    return newQuery;
  }

  public SQLQuery<T> leftOuterJoin( IType type) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._joinExpr =  SQLConstraint.join(type , "LEFT OUTER JOIN");
    return newQuery;
  }

  public SQLQuery<T> rightOuterJoin( IType type) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._joinExpr =  SQLConstraint.join(type , "RIGHT OUTER JOIN");
    return newQuery;
  }

  public SQLQuery<T> rightJoin( IType type) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._joinExpr =  SQLConstraint.join(type , "RIGHT JOIN");
    return newQuery;
  }

  public SQLQuery<T> leftJoin( IType type) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._joinExpr =  SQLConstraint.join(type , "LEFT JOIN");
    return newQuery;
  }

  public SQLQuery<T> naturalJoin( IType type) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._joinExpr =  SQLConstraint.join(type , "NATURAL JOIN");
    return newQuery;
  }


  public SQLQuery<T> on( SQLConstraint constraint) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._onExpr = SQLConstraint.on(constraint);
    newQuery._joinExpr = this._joinExpr;
    return newQuery;
  }

  public Iterator<T> iterator()
  {
    return execQuery().iterator();
  }

  private SQLQuery getRoot() {
    if(_parent != null) {
      return _parent.getRoot();
    } else {
      return this;
    }
  }

  public String  getSQLString() {
    String select =  "SELECT " +  _metadata.getTableForType( getRoot()._rootType ) + ".* ";
    String from = "FROM " + _metadata.getTableForType( getRoot()._rootType );
    String join = _joinExpr == null ? "" : _joinExpr.getSQL( _metadata);
    String on =  _onExpr == null ? "" : _onExpr.getSQL( _metadata);
    String where = _whereExpr == null ? "" : "WHERE " + _whereExpr.getSQL( _metadata );
    return select + " " +  from + " "  + join + on + " " + " " + where;
  }

  public List<Object> getArgs()
  {
    List answer = new ArrayList();
    if(_joinExpr!=null) {
      answer.addAll(_joinExpr.getArgs());
    }
    if(_onExpr!=null) {
      answer.addAll(_onExpr.getArgs());
    }
    if(_whereExpr!=null) {
      answer.addAll(_whereExpr.getArgs());
    }
    return answer;
  }

  private Iterable<T> execQuery()
  {
    try
    {
      return SQLRecord.select( getSQLString(), getArgs(), _rootType );
    }
    catch( SQLException e )
    {
      throw GosuExceptionUtil.forceThrow( e );
    }
  }
}