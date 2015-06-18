package ragnardb.plugin;

import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.PropertyInfoBase;
import ragnardb.runtime.SQLResult;

import java.util.Collections;
import java.util.List;

public class SQLColumnPropertyInfo extends PropertyInfoBase implements IPropertyInfo
{
  private String _columnName;
  private String _propName;
  private IType _propType;
  private IPropertyAccessor _accessor;

  protected SQLColumnPropertyInfo( String columnName, String propName, IType propertyType, ITypeInfo container )
  {
    super( container );
    _columnName = columnName;
    _propName = propName;
    _propType = propertyType;
    _accessor = new IPropertyAccessor()
    {
      @Override
      public Object getValue( Object obj )
      {
        return ((SQLResult)obj).getRawValue( _propName );
      }

      @Override
      public void setValue( Object obj, Object val )
      {
        ((SQLResult)obj).setRawValue( _propName, val );
      }
    };

  }

  public String getColumnName()
  {
    return _columnName;
  }

  @Override
  public boolean isReadable()
  {
    return true;
  }

  @Override
  public boolean isWritable( IType iType )
  {
    return true;
  }

  @Override
  public IPropertyAccessor getAccessor()
  {
    return _accessor;
  }

  @Override
  public List<IAnnotationInfo> getDeclaredAnnotations()
  {
    return Collections.emptyList();
  }

  @Override
  public String getName()
  {
    return _propName;
  }

  @Override
  public IType getFeatureType()
  {
    return _propType;
  }
}
