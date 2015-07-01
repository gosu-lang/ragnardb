package ragnardb.runtime;

import gw.lang.reflect.features.IPropertyReference;

import java.util.Arrays;
import java.util.List;

public abstract class SQLConstraint
{

  IPropertyReference _propertyReference;

  SQLConstraint( IPropertyReference pr )
  {
    _propertyReference = pr;
  }

  static SQLConstraint isEqualTo( IPropertyReference pr, Object o )
  {
    return new IsEqualToConstraint( pr, o );
  }

  static SQLConstraint isIn( IPropertyReference pr, List<Object> l )
  {
    return new IsInConstraint( pr, l );
  }

  static SQLConstraint isLike( IPropertyReference pr, String s )
  {
    return new IsLikeConstraint( pr, s );
  }

  abstract String getSQL( ITypeToSQLMetadata metadata );

  abstract List<Object> getArgs();

  private static class IsEqualToConstraint extends SQLConstraint
  {
    Object _obj;

    IsEqualToConstraint( IPropertyReference pr, Object o )
    {
      super( pr );
      _obj = o;
    }

    public String getSQL( ITypeToSQLMetadata metadata )
    {
      return metadata.getColumnForProperty( _propertyReference.getPropertyInfo() ) + "=?";
    }

    List<Object> getArgs()
    {
      return Arrays.asList( _obj );
    }
  }

  private static class IsInConstraint extends SQLConstraint
  {
    List _list;

    IsInConstraint( IPropertyReference pr, List list )
    {
      super( pr );
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
    String _str;

    IsLikeConstraint( IPropertyReference pr, String str )
    {
      super( pr );
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