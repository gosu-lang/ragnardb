package ragnardb.runtime;

import gw.lang.reflect.IType;
import gw.lang.reflect.features.PropertyReference;
import gw.util.GosuExceptionUtil;
import ragnardb.plugin.SQLColumnPropertyInfo;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by carson on 7/1/15.
 */
public class SQLQuery<T> implements Iterable<T>{

  protected IType _rootType;
  private SQLConstraint _whereExpr;
  private SQLConstraint _joinExpr;
  private SQLConstraint _onExpr;
  private String _select;
  private SQLQuery _parent;
  private PropertyReference _singlePick;
  protected ITypeToSQLMetadata _metadata;


  public SQLQuery( ITypeToSQLMetadata md, IType rootType )
  {
    _metadata = md;
    _rootType = rootType;
    _select = _metadata.getTableForType( getRoot()._rootType ) + ".* ";
  }

  public SQLQuery( ITypeToSQLMetadata md, IType rootType, PropertyReference p)
  {
    _metadata = md;
    _rootType = rootType;
    _select = _metadata.getTableForType( getRoot()._rootType ) + ".* ";

  }


  public SQLQuery<T> where(SQLConstraint constraint) {
    SQLQuery<T> newQuery = new SQLQuery<T>( _metadata, _rootType );
    if(this._whereExpr == null) {
      newQuery._whereExpr = constraint;
    }
    else{
      newQuery._whereExpr = this._whereExpr.andAlso(constraint);
    }
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

  public SQLQuery<T> pick( PropertyReference ref){

    SQLQuery base = this;
    this._select =    ((SQLColumnPropertyInfo) ref.getPropertyInfo()).getColumnName();
    this._singlePick = ref;



      return new SQLQuery(_metadata,_rootType){
          public Iterator<T> iterator()
          {
            List col = new ArrayList<>();
            for ( Object s : base){
              col.add(((SQLRecord) s).getRawValue(_select));
            }
            return col.iterator();
          }

          public List<Object> getArgs(){
            return base.getArgs();
          }

          public String  getSQLString() {
            return base.getSQLString();
          }
      };



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
    String select =  "SELECT " +  _select;
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