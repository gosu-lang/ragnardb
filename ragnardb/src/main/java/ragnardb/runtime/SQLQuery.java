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
  private String _groupBy;
  private SQLConstraint _orderByExpr;
  private SQLConstraint _limitExpr;
  private SQLConstraint _offsetExpr;
  private PropertyReference _pick;

  private void setManualSelect(String manualSelect){
    _manualSelect = manualSelect;
  }

  protected void setGroupBy(String str){
    _groupBy = str;
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
    PreparedStatement delete = RagnarDB.prepareStatement(from + " " + where, getArgs());
    return delete.execute();
  }

  public <U> SQLQuery<U> pick( PropertyReference<Object, U> ref)
  {
    SQLQuery<U> sqlQuery = (SQLQuery<U>) cloneMe();
    sqlQuery._pick = ref;
    return sqlQuery;
  }

  public SQLQuery<T> groupBy(PropertyReference<Object, Object> ref){
    SQLQuery<T> sqlQuery = (SQLQuery<T>) cloneMe();
    sqlQuery.setGroupBy( " GROUP BY( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + ") ");
    return sqlQuery;
  }

  public SQLQuery<T> count() {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " COUNT(*) " );
    return newQuery;
  }

  public SQLQuery<T> count( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " COUNT( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> max( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " MAX( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> min( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " MIN( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> sum( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " SUM( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> avg( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " AVG( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> countDistinct( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " COUNT( DISTINCT " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> maxDistinct( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " MAX( DISTINCT " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> minDistinct( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " MIN( DISTINCT " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> sumDistinct( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " SUM( DISTINCT " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> avgDistinct( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " AVG( DISTINCT " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> groupConcat( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " GROUP_CONCAT( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> groupConcatDistinct( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " GROUP_CONCAT( DISTINCT " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> listAgg( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " LISTAGG( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> listAgg( PropertyReference<Object, Object> ref, String str) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " LISTAGG( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() +
      " , " + str + " ) " );
    return newQuery;
  }

  public SQLQuery<T> median( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " MEDIAN( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> stddevPop( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " STDDEVPOP( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> stddevSamp( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " STDDEVSAMP( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> varPop( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " VARPOP( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
    return newQuery;
  }

  public SQLQuery<T> varSamp( PropertyReference<Object, Object> ref) {
    SQLQuery<T> newQuery = cloneMe();
    newQuery.setManualSelect( " VARSAMP( " + ((SQLColumnPropertyInfo)ref.getPropertyInfo()).getColumnName() + " ) " );
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
    String groupBy = _groupBy == null ? "" : _groupBy;
    String orderBy =  _orderByExpr == null ? "" :  _orderByExpr.getSQL(_metadata);
    String limit =  _limitExpr == null ? "" :  _limitExpr.getSQL(_metadata);
    String offset =  _offsetExpr == null ? "" :  _offsetExpr.getSQL(_metadata);
    return select + " " +  from + " "  + join + " " + " " + where + " " + groupBy + " " + orderBy + " " + limit + " " + offset;
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
    child._groupBy = this._groupBy;
    child._orderByExpr = this._orderByExpr;
    child._limitExpr = this._limitExpr;
    child._offsetExpr = this._offsetExpr;
    child._manualSelect = this._manualSelect;
    return child;
  }


}