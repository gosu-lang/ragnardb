package ragnardb.runtime;

import gw.lang.reflect.IPropertyInfo;

import java.util.Arrays;
import java.util.List;

public abstract class SQLConstraint
{

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

  private static class IsEqualToConstraint extends SQLConstraint
  {
    IPropertyInfo _propertyInfo;
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
    IPropertyInfo _propertyInfo;
    List _list;

    IsInConstraint( IPropertyInfo pi, List list )
    {
      _propertyInfo = pi;
      _list = list;
    }

    public String getSQL( ITypeToSQLMetadata metadata )
    {
      throw new UnsupportedOperationException( "Need to implement" );
    }

    List<Object> getArgs()
    {
      return _list;
    }
  }

  private static class IsLikeConstraint extends SQLConstraint
  {
    IPropertyInfo _propertyInfo;
    String _str;

    IsLikeConstraint( IPropertyInfo pi, String str )
    {
      _propertyInfo = pi;
      _str = str;
    }

    public String getSQL( ITypeToSQLMetadata metadata )
    {
      throw new UnsupportedOperationException( "Need to implement" );
    }

    List<Object> getArgs()
    {
      return Arrays.asList( _str );
    }
  }
}