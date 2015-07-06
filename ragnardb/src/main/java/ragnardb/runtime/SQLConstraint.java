package ragnardb.runtime;

import com.sun.tools.javac.util.StringUtils;
import gw.lang.reflect.IPropertyInfo;
import ragnardb.parser.ast.SQL;

import java.awt.font.NumericShaper;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  abstract String getSQL( ITypeToSQLMetadata metadata );

  abstract List<Object> getArgs();


  public SQLConstraint andAlso(SQLConstraint sql){
    return new AndConstraint(_propertyInfo, this, sql);
  }

  public SQLConstraint orElse(SQLConstraint sql){
    return new OrConstraint(_propertyInfo, this, sql);
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