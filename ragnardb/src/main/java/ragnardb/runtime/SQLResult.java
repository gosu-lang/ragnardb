package ragnardb.runtime;

import ragnardb.api.ISQLResult;

import java.util.HashMap;
import java.util.Map;

public class SQLResult implements ISQLResult
{
  private Map<String, Object> _values = new HashMap<>();

  @Override
  public Object getRawValue( String property )
  {
    return _values.get( property );
  }

  @Override
  public void setRawValue( String property, Object value )
  {
    _values.put( property, value );
  }
}
