package ragnardb.runtime;

import gw.lang.reflect.IType;
import gw.util.GosuExceptionUtil;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by carson on 7/1/15.
 */
public class SQLQuery<T> implements Iterable<T>{

  private IType _rootType;
  private SQLConstraint _whereExpr;
  private SQLConstraint _joinExpr;
  private SQLQuery _parent;
  private ITypeToSQLMetadata _metadata;

  public SQLQuery( ITypeToSQLMetadata md, IType rootType )
  {
    _metadata = md;
    _rootType = rootType;
  }

  public SQLQuery<T> where(SQLConstraint constraint) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._whereExpr = constraint;
    return newQuery;
  }

  public SQLQuery<T> join( SQLConstraint constraint) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    newQuery._joinExpr = constraint;
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
    String select =  "SELECT *";
    String from = "FROM " + _metadata.getTableForType( getRoot()._rootType );
    String where = _whereExpr == null ? "" : "WHERE " + _whereExpr.getSQL( _metadata );
    return select + " " + from + " " + where;
  }

  public List<Object> getArgs()
  {
    return _whereExpr.getArgs();
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