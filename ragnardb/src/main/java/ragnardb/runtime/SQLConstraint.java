package ragnardb.runtime;

import gw.lang.reflect.IFeatureInfo;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.features.PropertyReference;
import ragnardb.parser.ast.Constraint;
import ragnardb.plugin.ISQLTableType;

import java.util.*;

public abstract class SQLConstraint
{

  IPropertyInfo _propertyInfo;

  public static SQLConstraint isComparator( IPropertyInfo pr, Object o, String s )
  {
    return new ComparatorConstraint( pr, o , s );
  }

  public static SQLConstraint isIn( IPropertyInfo pr, List<Object> l )
  {
    return new IsInConstraint( pr, l );
  }

  public static SQLConstraint isIn( IPropertyInfo pr, SQLQuery<Object> s ) {
    return new IsInConstraint(pr, s);
  }

  public static SQLConstraint isLike( IPropertyInfo pr, String s )
  {
    return new IsLikeConstraint( pr, s );
  }

  public static SQLConstraint join( IType s , String joinName )
  {
    return new JoinConstraint( s , joinName );
  }

  public static SQLConstraint on( SQLConstraint s) {
    return new OnConstraint(s);
  }


  abstract String getSQL( ITypeToSQLMetadata metadata );

  abstract List<Object> getArgs();

  public SQLConstraint addOn(SQLConstraint sql){
    return new CombinedConstraint( this, sql);
  }

  public SQLConstraint andAlso(SQLConstraint sql){
    return new AndConstraint(_propertyInfo, this, sql);
  }

  public SQLConstraint orElse(SQLConstraint sql){
    return new OrConstraint(_propertyInfo, this, sql);
  }


  private static class CombinedConstraint extends SQLConstraint
  {
    SQLConstraint c1;
    SQLConstraint c2;

    CombinedConstraint( SQLConstraint _c1 , SQLConstraint _c2)
    {
      c1 = _c1;
      c2 = _c2;
    }


    public String getSQL( ITypeToSQLMetadata metadata  )
    {
      return c1.getSQL(metadata) + " " + c2.getSQL(metadata);
    }

    List<Object> getArgs()
    {
      List answer = new ArrayList();
      answer.addAll(c1.getArgs());
      answer.addAll(c2.getArgs());
      return answer;
    }
  }



  private static class JoinConstraint extends SQLConstraint
  {
    String _obj;
    String _joinType;

    JoinConstraint( IType o , String joinType)
    {
      _obj = ((ISQLTableType) o).getTable().getTableName();
      _joinType = joinType;
    }


    public String getSQL( ITypeToSQLMetadata metadata  )
    {
      return " " + _joinType + " ( " + _obj
         + " ) ";
    }

    List<Object> getArgs()
    {
      return Collections.emptyList();
    }
  }

  private static class AndConstraint extends SQLConstraint
  {

    SQLConstraint constraint1;
    SQLConstraint constraint2;
    String result;

    AndConstraint( IPropertyInfo pi, SQLConstraint _constraint1, SQLConstraint _constraint2 )
    {
      _propertyInfo = pi;
      constraint1 = _constraint1;
      constraint2 = _constraint2;
    }

    public String getSQL( ITypeToSQLMetadata metadata ){
      result = " ( " +   constraint1.getSQL(metadata)  + " AND " +  constraint2.getSQL(metadata) + " ) ";
      return result;
    }

    List<Object> getArgs()
    {
      List answer = new ArrayList();
      answer.addAll(constraint1.getArgs());
      answer.addAll(constraint2.getArgs());
      return answer;
    }
  }

  private static class OnConstraint extends SQLConstraint
  {

    SQLConstraint constraint1;


    OnConstraint( SQLConstraint _constraint1 )
    {
      constraint1 = _constraint1;
    }

    public String getSQL( ITypeToSQLMetadata metadata ){

      return " ON " + constraint1.getSQL( metadata);
    }

    List<Object> getArgs()
    {
      return constraint1.getArgs();
    }
  }

  private static class OrConstraint extends SQLConstraint
  {

    SQLConstraint constraint1;
    SQLConstraint constraint2;
    String result;

    OrConstraint( IPropertyInfo pi, SQLConstraint _constraint1, SQLConstraint _constraint2 )
    {
      _propertyInfo = pi;
      constraint1 = _constraint1;
      constraint2 = _constraint2;
    }

    public String getSQL( ITypeToSQLMetadata metadata ){
      result = " ( " +   constraint1.getSQL(metadata)  + " OR " +  constraint2.getSQL(metadata) + " ) ";
      return result;
    }

    List<Object> getArgs()
    {
      List answer = new ArrayList();
      answer.addAll(constraint1.getArgs());
      answer.addAll(constraint2.getArgs());
      return answer;
    }
  }

  private static class ComparatorConstraint extends SQLConstraint
  {
    List<Object> _objs;
    String RHS;
    String _comparator;

    ComparatorConstraint( IPropertyInfo pi, Object o, String comparator )
    {
      _propertyInfo = pi;
      _comparator = comparator;
      if( o instanceof PropertyReference){
        IFeatureInfo info = ((PropertyReference) o).getFeatureInfo();
        RHS = ((ISQLTableType) info.getOwnersType()).getTable().getTableName()+"."+info.getDisplayName();
        _objs = new ArrayList<>();
      }
      else {
        _objs = Arrays.asList( o );
        RHS = " ? ";
      }
    }

    public String getSQL( ITypeToSQLMetadata metadata )
    {
      return metadata.getColumnForProperty( _propertyInfo ) + _comparator + RHS;
    }

    List<Object> getArgs()
    {
      return _objs;
    }
  }

  private static class IsInConstraint extends SQLConstraint
  {
    List _list;
    SQLQuery _query;

    IsInConstraint( IPropertyInfo pi, List list )
    {
      _propertyInfo = pi;
      _list = list;
    }

    IsInConstraint( IPropertyInfo pi, SQLQuery query )
    {
      _propertyInfo = pi;
      _list = query.getArgs();
      _query = query;

    }

    public String getSQL( ITypeToSQLMetadata metadata )
    {
      if(_query!=null){
        return  metadata.getColumnForProperty(_propertyInfo)
        + " IN (" + _query.getSQLString() + ") ";
      }
      else {
        String ans = "";
        ans += metadata.getColumnForProperty(_propertyInfo);
        ans += " IN (";
        if (_list.size() != 0) {
          for (int x = 0; x < _list.size() - 1; x++) {
            ans += " ? ,";
          }
          ans += " ? ";
        }
        ans += " ) ";
        return ans;
      }
    }

    List<Object> getArgs()
    {
      return _list;
    }
  }

  private static class IsLikeConstraint extends SQLConstraint
  {
    String _str;

    IsLikeConstraint( IPropertyInfo pi, String str )
    {
      _propertyInfo = pi;
      _str = str;
    }

    public String getSQL( ITypeToSQLMetadata metadata )
    {
      return (metadata.getColumnForProperty(_propertyInfo) + " LIKE ?");
    }

    List<Object> getArgs()
    {
      return Arrays.asList( _str );
    }
  }
}