package ragnardb.plugin;

import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.util.GosuClassUtil;
import gw.util.concurrent.LockingLazyVar;

import java.sql.Types;
import java.util.Arrays;
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
    return GosuClassUtil.getShortClassName(getName());
  }

  @Override
  public String getNamespace() {
    return GosuClassUtil.getPackage(getName());
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

  public List<ColumnDefinition> getColumnDefinitions() {

    //TODO mock implementation; remove these once we hook up with the parser
    ColumnDefinition userId = new ColumnDefinition("UserId", Types.INTEGER);
    ColumnDefinition lastName = new ColumnDefinition("LastName", Types.NVARCHAR);
    ColumnDefinition firstName = new ColumnDefinition("FirstName", Types.NVARCHAR);
    ColumnDefinition age = new ColumnDefinition("Age", Types.INTEGER);
    final List<ColumnDefinition> contactCols = Arrays.asList(userId, lastName, firstName, age);

    switch(getName()) {
      case "ragnardb.foo.Users.Contacts": return contactCols;
      case "": return Collections.emptyList();
      default: throw new Error("Unknown sqlType: " + getName());
    }
  }

}
