package ragnardb.plugin;

import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.util.concurrent.LockingLazyVar;

import java.util.Collections;
import java.util.List;

public class SQLType extends TypeBase implements ISQLType {

  private final SQLPlugin _plugin;
  private final String _name;
  private LockingLazyVar<ITypeInfo> _typeInfo = new LockingLazyVar<ITypeInfo>()
  {
    @Override
    protected ITypeInfo init()
    {
      return new SQLTypeInfo( SQLType.this );
    }
  };

  public SQLType(SQLPlugin plugin, String name) {
    _plugin = plugin;
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getRelativeName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getNamespace() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public ITypeLoader getTypeLoader() {
    return _plugin;
  }

  @Override
  public IType getSupertype() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public IType[] getInterfaces() {
    return new IType[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public ITypeInfo getTypeInfo() {
    return _typeInfo.get();
  }

  @Override
  public List<ColumnDefinition> getColumnDefinitions()
  {
    return Collections.emptyList();
  }
}
