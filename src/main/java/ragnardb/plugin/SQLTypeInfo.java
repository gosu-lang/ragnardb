package ragnardb.plugin;

import gw.lang.reflect.BaseTypeInfo;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.java.JavaTypes;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLTypeInfo extends BaseTypeInfo
{
  private List<IPropertyInfo> _propertiesList;
  private Map<String, IPropertyInfo> _propertiesMap;

  public SQLTypeInfo( ISQLType type )
  {
    super( type );
    resolveProperties(type);
  }

  private void resolveProperties( ISQLType type )
  {
    _propertiesList = new ArrayList<>();
    _propertiesMap = new HashMap<>();
    List<ColumnDefinition> columns = type.getColumnDefinitions();
    for( ColumnDefinition column : columns )
    {
      SQLColumnPropertyInfo prop = new SQLColumnPropertyInfo( column.getColumnName(),
                                                              makePropertyName( column.getColumnName() ),
                                                              getGosuType( column.getSQLType() ), this );
      _propertiesMap.put(prop.getName(), prop);
      _propertiesList.add(prop);
    }
  }

  private IType getGosuType( int sqlType )
  {
    if( sqlType == Types.VARCHAR )
    {
      return JavaTypes.STRING();
    }
    else
    {
      return JavaTypes.OBJECT();
    }
  }

  private String makePropertyName( String columnName )
  {
    return columnName;
  }

  @Override
  public List<? extends IPropertyInfo> getProperties()
  {
    return _propertiesList;
  }

  @Override
  public IPropertyInfo getProperty( CharSequence propName )
  {
    return _propertiesMap.get( propName );
  }
}
