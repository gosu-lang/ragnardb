package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.features.PropertyReference;
import gw.lang.reflect.java.GosuTypes;
import gw.lang.reflect.java.JavaTypes;
import gw.util.GosuExceptionUtil;
import ragnardb.RagnarDB;
import ragnardb.parser.ast.SQL;
import ragnardb.plugin.SQLColumnPropertyInfo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Created by carson on 7/1/15.
 */
public class SQLQuery<T> implements Iterable<T> {

  protected ITypeToSQLMetadata _metadata;
  protected IType _rootType;
  protected IType _replaceType;
  private String _manualSelect;
  private SQLConstraint _whereExpr;
  private SQLConstraint _joinExpr; // Includes On expressions as well!
  private SQLConstraint _orderByExpr;
  private SQLConstraint _limitExpr;
  private SQLConstraint _offsetExpr;
  private PropertyReference _pick;

  private void setManualSelect(String manualSelect){
    _manualSelect = manualSelect;
  }

  protected void setType(IType type){
    _replaceType = type;
  }

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

  public SQLQuery<T> limit(int i){
    SQLQuery<T> newQuery = cloneMe();
    newQuery._limitExpr = SQLConstraint.limit(i);
    return newQuery;
  }

  public SQLQuery<T> offset(int i){
    SQLQuery<T> newQuery = cloneMe();
    newQuery._offsetExpr = SQLConstraint.offset(i);
    return newQuery;
  }

  public SQLQuery<T> union( SQLQuery query) {
    return new SetOpQuery(this, query, " UNION " , _metadata , _rootType);
  }

  public SQLQuery<T> unionAll( SQLQuery query) {
    return new SetOpQuery(this, query, " UNION ALL " , _metadata , _rootType);
  }

  public SQLQuery<T> intersect( SQLQuery query) {
    return new SetOpQuery(this, query, " INTERSECT " , _metadata , _rootType);
  }

  public SQLQuery<T> except( SQLQuery query) {
    return new SetOpQuery(this, query, " EXCEPT " , _metadata , _rootType);
  }




  public boolean delete() throws SQLException
  {
    String from = "DELETE FROM " + _metadata.getTableForType(_rootType);
    String where = _whereExpr == null ? "" : "WHERE " + _whereExpr.getSQL( _metadata );
    PreparedStatement delete = RagnarDB.prepareStatement( from + " " + where, getArgs() );
    return delete.execute();
  }

  public <U> SQLQuery<U> pick( PropertyReference<Object, U> ref)
  {
    SQLQuery<U> sqlQuery = (SQLQuery<U>) cloneMe();
    sqlQuery._pick = ref;
    return sqlQuery;
  }

  public SQLQuery<T> count( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " COUNT( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public Iterator<T> iterator()
  {
    Iterator<T> result;
    try
    {
      if( (_pick != null) || (_manualSelect != null) ) {
        result = SQLRecord.selectSingleColumn( getSQLString(), getArgs() );
      } else {
        result = SQLRecord.select( getSQLString(), getArgs(), _rootType );
      }
    }
    catch( SQLException e )
    {
      throw GosuExceptionUtil.forceThrow( e );
    }
    return result;
  }

  public String  getSQLString() {
    String select =  "SELECT " + getSelect();
    String from = "FROM " + _metadata.getTableForType(_rootType);
    String join = _joinExpr == null ? "" : _joinExpr.getSQL( _metadata);
    String where = _whereExpr == null ? "" : "WHERE " + _whereExpr.getSQL( _metadata );
    String orderBy =  _orderByExpr == null ? "" :  _orderByExpr.getSQL(_metadata);
    String limit =  _limitExpr == null ? "" :  _limitExpr.getSQL(_metadata);
    String offset =  _offsetExpr == null ? "" :  _offsetExpr.getSQL(_metadata);
    return select + " " +  from + " "  + join + " " + " " + where + " " + orderBy + " " + limit + " " + offset;
  }



  //--------------------------------------------------------------------------------
  // Implementation
  //--------------------------------------------------------------------------------


  private static class SetOpQuery extends SQLQuery
  {


    private final SQLQuery query1;
    private final SQLQuery query2;
    private final String opString;

    SetOpQuery(SQLQuery query1, SQLQuery query2, String opString, ITypeToSQLMetadata  metadata,IType rootType)
    {
      super(metadata,rootType);
      this.query1 = query1;
      this.query2 = query2;
      this.opString = opString;
    }

    @Override
    public String getSQLString()
    {
      return query1.getSQLString() + opString + query2.getSQLString();
    }

    @Override
    public List<Object> getArgs()
    {
      List ans = new ArrayList<>();
      ans.addAll(query1.getArgs());
      ans.addAll(query2.getArgs());
      return ans;

    }
  }

  private String getSelect()
  {
    if( _pick != null )
    {
      return ((SQLColumnPropertyInfo)_pick.getPropertyInfo()).getColumnName();
    }
    else if ( _manualSelect != null){
      return _manualSelect;
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
    child._limitExpr = this._limitExpr;
    child._offsetExpr = this._offsetExpr;
    child._manualSelect = this._manualSelect;
    return child;
  }


}