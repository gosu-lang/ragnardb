package ragnardb.api;

import gw.lang.reflect.features.IPropertyReference;

public interface ISQLResult
{
  Object getRawValue( String property );
  void setRawValue( String property, Object value );
}
