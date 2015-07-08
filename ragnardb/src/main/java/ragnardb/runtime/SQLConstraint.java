package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import ragnardb.plugin.ISQLTableType;

import java.util.*;

public abstract class SQLConstraint
{

  IPropertyInfo _propertyInfo;

  public static SQLConstraint isEqualTo( IPropertyInfo pr, Object o )
  {
    return new IsEqualToConstraint( pr, o );
  }

  public static SQLConstraint isIn( IPropertyInfo pr, List<Object> l )
  {
    return new IsInConstraint( pr, l );
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


  public SQLConstraint andAlso(SQLConstraint sql){
    return new AndConstraint(_propertyInfo, this, sql);
  }

  public SQLConstraint orElse(SQLConstraint sql){
    return new OrConstraint(_propertyInfo, this, sql);
  }

  private static class JoinConstraint extends SQLConstraint
  {
    IType _obj;
    String _joinType;

    JoinConstraint( IType o , String joinType)
    {
      _obj = o;
      _joinType = joinType;
    }


    public String getSQL( ITypeToSQLMetadata metadata  )
    {
      return " " + _joinType + " " +
        ((ISQLTableType) _obj).getTable().getTableName() + " ";
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

  private static class IsEqualToConstraint extends SQLConstraint
  {
    Object _obj;

    IsEqualToConstraint( IPropertyInfo pi, Object o )
    {
      _propertyInfo = pi;
      _obj = o;
    }

    public String getSQL( ITypeToSQLMetadata metadata )
    {
      return metadata.getColumnForProperty( _propertyInfo ) + "=?";
    }

    List<Object> getArgs()
    {
      return Arrays.asList( _obj );
    }
  }

  private static class IsInConstraint extends SQLConstraint
  {
    List _list;

    IsInConstraint( IPropertyInfo pi, List list )
    {
      _propertyInfo = pi;
      _list = list;
    }

    public String getSQL( ITypeToSQLMetadata metadata )
    {
      String ans = "";
      ans += metadata.getColumnForProperty(_propertyInfo);
      ans += " IN (";
      if(_list.size() != 0) {
        for (int x = 0; x < _list.size() - 1; x++){
          ans += " ? ,";
        }
        ans += " ? ";
      }
      ans += " ) ";
      return ans;
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